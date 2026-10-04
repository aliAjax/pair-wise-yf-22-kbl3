package com.generated.qualityTrace.services;

import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.generated.qualityTrace.constants.DefectSeverity;
import com.generated.qualityTrace.constants.InspectionResultStatus;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.exceptions.NotFoundException;
import com.generated.qualityTrace.exceptions.RecalculationException;
import com.generated.qualityTrace.middlewares.AuditLogMiddleware;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.repositories.DefectRecordRepository;
import com.generated.qualityTrace.repositories.InspectionItemResultRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.utils.UserContext;

/**
 * 批次结论重算服务。
 *
 * 核心规则：
 * 1. 批次结论不再由各次写入拼接，而是按已生效的检验项与不良记录整体重算；
 * 2. 同一检验项重复提交只留最新一次（effective=true），重算只读已生效集合；
 * 3. 检验项一更新，旧结论即整体重算（非增量补丁）；
 * 4. 同一批次并发提交通过 SELECT ... FOR UPDATE 在数据库层面串行化，只让一次重算真正写结论；
 * 5. 重算失败按批次号重试，重试耗尽保留上一次结论（不覆盖）。
 */
@Service
public class BatchConclusionService {

  private static final Logger log = LoggerFactory.getLogger(BatchConclusionService.class);

  private final ProductBatchRepository batchRepo;
  private final InspectionItemResultRepository itemRepo;
  private final DefectRecordRepository defectRepo;
  private final AuditLogMiddleware auditLog;
  private final TransactionTemplate txTemplate;

  @Value("${recalc.max-attempts:3}")
  private int maxAttempts;

  @Value("${recalc.backoff-ms:200}")
  private long backoffMs;

  public BatchConclusionService(ProductBatchRepository batchRepo,
                                InspectionItemResultRepository itemRepo,
                                DefectRecordRepository defectRepo,
                                AuditLogMiddleware auditLog,
                                TransactionTemplate txTemplate) {
    this.batchRepo = batchRepo;
    this.itemRepo = itemRepo;
    this.defectRepo = defectRepo;
    this.auditLog = auditLog;
    this.txTemplate = txTemplate;
  }

  /**
   * 按批次号重算（加行锁），加入调用方事务。供外部直接调用。
   */
  @Transactional
  public void recalculateByBatchNo(String batchNo) {
    ProductBatch batch = batchRepo.lockByBatchNo(batchNo);
    if (batch == null) {
      throw new NotFoundException("batch not found: " + batchNo);
    }
    computeAndStore(batch);
  }

  /**
   * 重算失败后按批次号重试，保留上一次结论。
   * 每次重试通过 TransactionTemplate 独立事务执行，避免 self-invocation 导致 @Transactional 失效，
   * 也避免被标记 rollback-only 后无法重试。
   */
  public void recalculateWithRetry(String batchNo) {
    int attempt = 0;
    while (true) {
      attempt++;
      log.info(LogTemplates.RECALC_START, batchNo, attempt);
      try {
        final int current = attempt;
        txTemplate.executeWithoutResult(status -> {
          ProductBatch batch = batchRepo.lockByBatchNo(batchNo);
          if (batch == null) {
            throw new NotFoundException("batch not found: " + batchNo);
          }
          computeAndStore(batch);
        });
        log.info(LogTemplates.RECALC_SUCCESS, batchNo, "OK", attempt);
        return;
      } catch (Exception e) {
        log.warn(LogTemplates.RECALC_FAIL, batchNo, attempt, e.getMessage());
        if (attempt >= maxAttempts) {
          // 重算失败：上一次结论未被覆盖，保留旧结论
          throw new RecalculationException(
              "batch conclusion recalculation failed after " + maxAttempts + " attempts for batch=" + batchNo
                  + ", previous conclusion preserved", e);
        }
        log.info(LogTemplates.RECALC_RETRY, batchNo, attempt, backoffMs);
        sleep(backoffMs);
      }
    }
  }

  /**
   * 读取已生效检验项 + 不良记录，整体重算结论并写回。
   * 只在计算成功后写结论，失败时不写（保留上一次结论）。
   */
  private void computeAndStore(ProductBatch batch) {
    List<InspectionItemResult> items = itemRepo.findEffectiveByBatchId(batch.getId());
    List<DefectRecord> defects = defectRepo.findByBatchId(batch.getId());
    String conclusion = computeConclusion(items, defects, Boolean.TRUE.equals(batch.getConcessionGranted()));

    String now = Instant.now().toString();
    batch.setConclusionStatus(conclusion);
    batch.setConclusionUpdatedAt(now);
    batchRepo.updateById(batch);

    auditLog.log("RECALCULATE", "BATCH", batch.getBatchNo(),
        "conclusion=" + conclusion + " items=" + items.size() + " defects=" + defects.size());
    log.info(LogTemplates.BATCH_CONCLUSION, batch.getBatchNo(), conclusion, items.size(), defects.size(),
        UserContext.getUsername());
  }

  /**
   * 结论判定规则：
   * - 任一已生效检验项 FAIL，或 存在未关闭的 CRITICAL 不良且未让步放行 → FAIL；
   * - 否则存在条件放行/复检项，或 存在未关闭不良，或 已让步放行且有不良 → CONDITIONAL_PASS；
   * - 其余 → PASS。
   */
  static String computeConclusion(List<InspectionItemResult> items, List<DefectRecord> defects,
                                  boolean concessionGranted) {
    boolean hasFailItem = items.stream()
        .anyMatch(it -> InspectionResultStatus.FAIL.equals(it.getItemStatus()));
    boolean hasCondItem = items.stream()
        .anyMatch(it -> InspectionResultStatus.CONDITIONAL_PASS.equals(it.getItemStatus())
            || InspectionResultStatus.RECHECK.equals(it.getItemStatus()));
    boolean hasOpenCritical = defects.stream()
        .anyMatch(d -> "OPEN".equals(d.getDispositionStatus())
            && DefectSeverity.CRITICAL.equals(d.getSeverity()));
    boolean hasOpenMajor = defects.stream()
        .anyMatch(d -> "OPEN".equals(d.getDispositionStatus())
            && DefectSeverity.MAJOR.equals(d.getSeverity()));
    boolean hasOpenMinor = defects.stream()
        .anyMatch(d -> "OPEN".equals(d.getDispositionStatus())
            && DefectSeverity.MINOR.equals(d.getSeverity()));
    boolean hasAnyDefect = !defects.isEmpty();

    if (hasFailItem) {
      return InspectionResultStatus.FAIL;
    }
    if (hasOpenCritical && !concessionGranted) {
      return InspectionResultStatus.FAIL;
    }
    if (hasOpenMajor && !concessionGranted) {
      return InspectionResultStatus.CONDITIONAL_PASS;
    }
    if (hasCondItem || hasOpenMinor || (concessionGranted && hasAnyDefect)) {
      return InspectionResultStatus.CONDITIONAL_PASS;
    }
    return InspectionResultStatus.PASS;
  }

  private void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }
}
