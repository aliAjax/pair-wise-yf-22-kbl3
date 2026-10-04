package com.generated.qualityTrace.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.generated.qualityTrace.constants.DefectSeverity;
import com.generated.qualityTrace.constants.DispositionStatus;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constructors.DefectRecordDtoFactory;
import com.generated.qualityTrace.constructors.ProductBatchDtoFactory;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.repositories.DefectRecordRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.types.ApiException;
import com.generated.qualityTrace.types.DefectRecordPayload;
import com.generated.qualityTrace.utils.Formatters;
import com.generated.qualityTrace.validators.PayloadValidator;

@Service
public class DefectRecordService {
  private static final Logger log = LoggerFactory.getLogger(DefectRecordService.class);

  private final DefectRecordRepository defects;
  private final ProductBatchRepository batches;
  private final BatchConclusionService conclusionService;

  public DefectRecordService(DefectRecordRepository defects, ProductBatchRepository batches,
      BatchConclusionService conclusionService) {
    this.defects = defects;
    this.batches = batches;
    this.conclusionService = conclusionService;
  }

  public List<Map<String, Object>> list() {
    return defects.findAll().stream().map(DefectRecordDtoFactory::toResponse).toList();
  }

  /** Registers a defect as OPEN, then recomputes the batch conclusion. */
  public Map<String, Object> register(DefectRecordPayload p) {
    validate(p);
    return conclusionService.inBatchLock(p.batchNo(), () -> {
      ProductBatch batch = batches.findByBatchNo(p.batchNo())
          .orElseThrow(() -> new ApiException(ErrorCodes.BATCH_NOT_FOUND,
              ErrorMessages.BATCH_NOT_FOUND + p.batchNo(), 404));
      DefectRecord d = new DefectRecord();
      d.batchId = String.valueOf(batch.id);
      d.defectType = p.defectType();
      d.defectQty = p.defectQty();
      d.severity = p.severity();
      d.rootCause = p.rootCause();
      d.dispositionStatus = DispositionStatus.OPEN.name();
      d.createdAt = Formatters.nowIso();
      defects.save(d);
      log.info(LogTemplates.DEFECT_REGISTER, p.batchNo(), p.defectType(), p.severity());
      ProductBatch rebased = conclusionService.recomputeLocked(p.batchNo());
      Map<String, Object> resp = new LinkedHashMap<>();
      resp.put("defect", DefectRecordDtoFactory.toResponse(d));
      resp.put("batch", ProductBatchDtoFactory.toResponse(rebased));
      return resp;
    });
  }

  /** Closes a defect (quality manager only, enforced at the controller). */
  public Map<String, Object> close(Long id, String actor) {
    return dispose(id, actor, DispositionStatus.CLOSED, LogTemplates.DEFECT_CLOSE);
  }

  /** Releases the batch under concession for this defect (quality manager only). */
  public Map<String, Object> concession(Long id, String actor) {
    return dispose(id, actor, DispositionStatus.CONCESSION, LogTemplates.DEFECT_CONCESSION);
  }

  private Map<String, Object> dispose(Long id, String actor, DispositionStatus target, String template) {
    DefectRecord d = defects.findById(id)
        .orElseThrow(() -> new ApiException(ErrorCodes.DEFECT_NOT_FOUND,
            ErrorMessages.DEFECT_NOT_FOUND + id, 404));
    ProductBatch batch = batches.findById(PayloadValidator.requireNumericId("batchId", d.batchId))
        .orElseThrow(() -> new ApiException(ErrorCodes.BATCH_NOT_FOUND,
            ErrorMessages.BATCH_NOT_FOUND + d.batchId, 404));
    return conclusionService.inBatchLock(batch.batchNo, () -> {
      d.dispositionStatus = target.name();
      defects.save(d);
      log.info(template, id, actor);
      ProductBatch rebased = conclusionService.recomputeLocked(batch.batchNo);
      Map<String, Object> resp = new LinkedHashMap<>();
      resp.put("defect", DefectRecordDtoFactory.toResponse(d));
      resp.put("batch", ProductBatchDtoFactory.toResponse(rebased));
      return resp;
    });
  }

  private void validate(DefectRecordPayload p) {
    if (p == null) {
      throw new ApiException(ErrorCodes.VALIDATION_FAILED, ErrorMessages.VALIDATION_FAILED + "body required", 400);
    }
    PayloadValidator.requireNonBlank("batchNo", p.batchNo());
    PayloadValidator.requireNonBlank("defectType", p.defectType());
    PayloadValidator.requireNonBlank("defectQty", p.defectQty());
    PayloadValidator.requireEnum(DefectSeverity.class, "severity", p.severity());
  }
}
