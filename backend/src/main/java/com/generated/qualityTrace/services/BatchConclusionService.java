package com.generated.qualityTrace.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.generated.qualityTrace.constants.BatchConclusion;
import com.generated.qualityTrace.constants.DefectSeverity;
import com.generated.qualityTrace.constants.DispositionStatus;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.InspectionResultStatus;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.repositories.DefectRecordRepository;
import com.generated.qualityTrace.repositories.InspectionItemResultRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.repositories.QualityInspectionRepository;
import com.generated.qualityTrace.types.ApiException;
import com.generated.qualityTrace.utils.Formatters;

/**
 * Derives the batch conclusion from the effective inspection items and defect records.
 * All writes that affect a batch conclusion go through a per-batch lock so that
 * concurrent submissions on the same batch let only one recompute take effect.
 */
@Service
public class BatchConclusionService {
  private static final Logger log = LoggerFactory.getLogger(BatchConclusionService.class);

  private final ProductBatchRepository batches;
  private final QualityInspectionRepository inspections;
  private final InspectionItemResultRepository items;
  private final DefectRecordRepository defects;
  private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

  public BatchConclusionService(ProductBatchRepository batches, QualityInspectionRepository inspections,
      InspectionItemResultRepository items, DefectRecordRepository defects) {
    this.batches = batches;
    this.inspections = inspections;
    this.items = items;
    this.defects = defects;
  }

  /**
   * Runs the write + recompute atomically per batch. A concurrent submission on the
   * same batch is rejected with 409 so only one recompute takes effect at a time.
   */
  public <T> T inBatchLock(String batchNo, Supplier<T> action) {
    ReentrantLock lock = locks.computeIfAbsent(batchNo, k -> new ReentrantLock());
    if (!lock.tryLock()) {
      log.warn(LogTemplates.CONCLUSION_RECOMPUTE_BUSY, batchNo);
      throw new ApiException(ErrorCodes.RECOMPUTE_IN_PROGRESS,
          ErrorMessages.RECOMPUTE_IN_PROGRESS + batchNo, 409);
    }
    try {
      return action.get();
    } finally {
      lock.unlock();
    }
  }

  /** Retry entry point after a failed recompute: recomputes the conclusion by batch number. */
  public ProductBatch recompute(String batchNo) {
    return inBatchLock(batchNo, () -> recomputeLocked(batchNo));
  }

  /**
   * Recomputes the conclusion from the effective items and defects. Must be called
   * inside inBatchLock. On failure the previous conclusion is kept untouched and the
   * error is recorded on the batch so the caller can retry by batch number.
   */
  public ProductBatch recomputeLocked(String batchNo) {
    ProductBatch batch = batches.findByBatchNo(batchNo)
        .orElseThrow(() -> new ApiException(ErrorCodes.BATCH_NOT_FOUND,
            ErrorMessages.BATCH_NOT_FOUND + batchNo, 404));
    try {
      List<QualityInspection> batchInspections = inspections.findByBatchId(String.valueOf(batch.id));
      List<String> inspectionIds = batchInspections.stream().map(q -> String.valueOf(q.id)).toList();
      List<InspectionItemResult> effectiveItems = items.findEffectiveByInspectionIds(inspectionIds);
      List<DefectRecord> batchDefects = defects.findByBatchId(String.valueOf(batch.id));
      String conclusion = compute(effectiveItems, batchDefects);
      batch.conclusion = conclusion;
      batch.conclusionVersion += 1;
      batch.conclusionUpdatedAt = Formatters.nowIso();
      batch.lastRecomputeError = null;
      batches.save(batch);
      log.info(LogTemplates.CONCLUSION_RECOMPUTE_OK, batchNo, conclusion, batch.conclusionVersion);
      return batch;
    } catch (RuntimeException e) {
      batch.lastRecomputeError = String.valueOf(e.getMessage());
      batches.save(batch);
      log.error(LogTemplates.CONCLUSION_RECOMPUTE_FAIL, batchNo, e.getMessage());
      throw new ApiException(ErrorCodes.RECOMPUTE_FAILED,
          ErrorMessages.RECOMPUTE_FAILED + batchNo, 500);
    }
  }

  private String compute(List<InspectionItemResult> effectiveItems, List<DefectRecord> batchDefects) {
    boolean anyItemFail = effectiveItems.stream()
        .anyMatch(i -> InspectionResultStatus.FAIL.name().equals(i.itemStatus));
    boolean anyItemRecheck = effectiveItems.stream()
        .anyMatch(i -> InspectionResultStatus.RECHECK.name().equals(i.itemStatus));
    boolean openMajorDefect = batchDefects.stream()
        .anyMatch(d -> isOpen(d) && isMajor(d) && qtyPositive(d));
    boolean concession = batchDefects.stream()
        .anyMatch(d -> DispositionStatus.CONCESSION.name().equals(d.dispositionStatus));
    boolean openMinorDefect = batchDefects.stream()
        .anyMatch(d -> isOpen(d) && !isMajor(d) && qtyPositive(d));
    if (anyItemFail || openMajorDefect) {
      return BatchConclusion.FAIL.name();
    }
    if (anyItemRecheck || effectiveItems.isEmpty()) {
      return BatchConclusion.PENDING.name();
    }
    if (concession || openMinorDefect) {
      return BatchConclusion.CONDITIONAL_PASS.name();
    }
    return BatchConclusion.PASS.name();
  }

  private boolean isOpen(DefectRecord d) {
    return DispositionStatus.OPEN.name().equals(d.dispositionStatus);
  }

  private boolean isMajor(DefectRecord d) {
    return DefectSeverity.MAJOR.name().equals(d.severity) || DefectSeverity.CRITICAL.name().equals(d.severity);
  }

  private boolean qtyPositive(DefectRecord d) {
    return Integer.parseInt(d.defectQty.trim()) > 0;
  }
}
