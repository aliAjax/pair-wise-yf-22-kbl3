package com.generated.qualityTrace.services;

import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import com.generated.qualityTrace.constants.InspectionResultStatus;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.exceptions.NotFoundException;
import com.generated.qualityTrace.exceptions.ValidationException;
import com.generated.qualityTrace.middlewares.AuditLogMiddleware;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.repositories.InspectionItemResultRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.repositories.QualityInspectionRepository;
import com.generated.qualityTrace.types.InspectionItemPayload;
import com.generated.qualityTrace.types.LoginUser;
import com.generated.qualityTrace.types.QualityInspectionPayload;
import com.generated.qualityTrace.utils.UserContext;

/**
 * 质量检验服务。质检员提交检验与检验项；同一检验项重复提交只留最新一次，
 * 提交后触发批次结论重算（失败按批次号重试，保留上一次结论）。
 */
@Service
public class QualityInspectionService {

  private static final Logger log = LoggerFactory.getLogger(QualityInspectionService.class);

  private final QualityInspectionRepository inspectionRepo;
  private final InspectionItemResultRepository itemRepo;
  private final ProductBatchRepository batchRepo;
  private final BatchConclusionService conclusionService;
  private final AuditLogMiddleware auditLog;
  private final TransactionTemplate txTemplate;

  public QualityInspectionService(QualityInspectionRepository inspectionRepo,
                                  InspectionItemResultRepository itemRepo,
                                  ProductBatchRepository batchRepo,
                                  BatchConclusionService conclusionService,
                                  AuditLogMiddleware auditLog,
                                  TransactionTemplate txTemplate) {
    this.inspectionRepo = inspectionRepo;
    this.itemRepo = itemRepo;
    this.batchRepo = batchRepo;
    this.conclusionService = conclusionService;
    this.auditLog = auditLog;
    this.txTemplate = txTemplate;
  }

  public List<QualityInspection> list() {
    return inspectionRepo.selectList(null);
  }

  /**
   * 提交检验（含检验项），成功后触发批次结论重算。
   * 重算失败不影响已保存的检验项，按批次号重试并保留上一次结论。
   */
  public Long submit(QualityInspectionPayload payload) {
    LoginUser actor = UserContext.get();
    if (actor == null) {
      throw new ValidationException("login required");
    }
    validate(payload);
    // 单次提交（加批次行锁 -> 建检验单 -> 逐项 upsert）在独立事务中完成
    Long inspectionId = txTemplate.execute(status -> submitOnce(payload, actor.userId()));
    // 检验项已落库；触发重算（失败重试，保留上一次结论）
    try {
      conclusionService.recalculateWithRetry(payload.batchNo());
    } catch (Exception e) {
      log.error("recalc failed after retries for batch={}, previous conclusion preserved", payload.batchNo(), e);
    }
    return inspectionId;
  }

  /**
   * 单次提交：加批次行锁 -> 建检验单 -> 逐项 upsert（旧记录置失效，只留最新一次）。
   * 必须在事务中调用（由 txTemplate 包裹）；事务提交后检验项即生效，重算在事务外按批次号重试。
   */
  public Long submitOnce(QualityInspectionPayload payload, Long inspectorId) {
    ProductBatch batch = batchRepo.lockByBatchNo(payload.batchNo());
    if (batch == null) {
      throw new NotFoundException("batch not found: " + payload.batchNo());
    }

    QualityInspection qi = new QualityInspection();
    qi.setBatchId(batch.getId());
    qi.setInspectorId(inspectorId);
    qi.setInspectionType(payload.inspectionType());
    qi.setStandardVersion(payload.standardVersion());
    qi.setInspectedAt(payload.inspectedAt() == null ? Instant.now().toString() : payload.inspectedAt());
    qi.setResultStatus(aggregateResult(payload.items()));
    inspectionRepo.insert(qi);

    String now = Instant.now().toString();
    for (InspectionItemPayload it : payload.items()) {
      // 同一检验项重复提交：旧记录置 effective=false
      itemRepo.markSuperseded(batch.getId(), it.itemCode());
      int nextVersion = itemRepo.maxVersion(batch.getId(), it.itemCode()) + 1;

      InspectionItemResult row = new InspectionItemResult();
      row.setInspectionId(qi.getId());
      row.setBatchId(batch.getId());
      row.setItemCode(it.itemCode());
      row.setItemName(it.itemName());
      row.setMeasuredValue(it.measuredValue());
      row.setLimitMin(it.limitMin());
      row.setLimitMax(it.limitMax());
      row.setItemStatus(it.itemStatus());
      row.setVersion(nextVersion);
      row.setEffective(true);
      row.setSubmittedAt(now);
      itemRepo.insert(row);

      auditLog.log("ITEM_SUBMIT", "INSPECTION_ITEM", it.itemCode(),
          "batch=" + payload.batchNo() + " version=" + nextVersion + " status=" + it.itemStatus());
      log.info(LogTemplates.ITEM_SUBMIT, payload.batchNo(), it.itemCode(), nextVersion,
          UserContext.getUsername());
    }

    auditLog.log("QI_SUBMIT", "QUALITY_INSPECTION", String.valueOf(qi.getId()),
        "batch=" + payload.batchNo() + " type=" + payload.inspectionType());
    log.info(LogTemplates.QI_SUBMIT, payload.batchNo(), qi.getId(), payload.inspectionType(),
        UserContext.getUsername());
    return qi.getId();
  }

  private void validate(QualityInspectionPayload payload) {
    if (payload == null || payload.batchNo() == null || payload.batchNo().isBlank()) {
      throw new ValidationException("batchNo is required");
    }
    if (payload.items() == null || payload.items().isEmpty()) {
      throw new ValidationException("at least one inspection item is required");
    }
    for (InspectionItemPayload it : payload.items()) {
      if (it.itemCode() == null || it.itemCode().isBlank()) {
        throw new ValidationException("itemCode is required");
      }
      if (it.itemStatus() == null || it.itemStatus().isBlank()) {
        throw new ValidationException("itemStatus is required for " + it.itemCode());
      }
    }
  }

  /** 检验单结论：任一检验项 FAIL 则 FAIL，否则有条件放行则 CONDITIONAL_PASS，否则 PASS */
  static String aggregateResult(List<InspectionItemPayload> items) {
    boolean anyFail = items.stream().anyMatch(it -> InspectionResultStatus.FAIL.equals(it.itemStatus()));
    boolean anyCond = items.stream().anyMatch(it -> InspectionResultStatus.CONDITIONAL_PASS.equals(it.itemStatus())
        || InspectionResultStatus.RECHECK.equals(it.itemStatus()));
    if (anyFail) {
      return InspectionResultStatus.FAIL;
    }
    if (anyCond) {
      return InspectionResultStatus.CONDITIONAL_PASS;
    }
    return InspectionResultStatus.PASS;
  }
}
