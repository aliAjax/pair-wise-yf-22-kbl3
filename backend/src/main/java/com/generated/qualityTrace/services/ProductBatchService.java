package com.generated.qualityTrace.services;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.generated.qualityTrace.constants.BatchConclusion;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constructors.DefectRecordDtoFactory;
import com.generated.qualityTrace.constructors.InspectionItemResultDtoFactory;
import com.generated.qualityTrace.constructors.ProductBatchDtoFactory;
import com.generated.qualityTrace.constructors.QualityInspectionDtoFactory;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.repositories.DefectRecordRepository;
import com.generated.qualityTrace.repositories.InspectionItemResultRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.repositories.QualityInspectionRepository;
import com.generated.qualityTrace.types.ApiException;
import com.generated.qualityTrace.types.ProductBatchPayload;
import com.generated.qualityTrace.utils.Formatters;
import com.generated.qualityTrace.validators.PayloadValidator;

@Service
public class ProductBatchService {
  private static final Logger log = LoggerFactory.getLogger(ProductBatchService.class);

  private final ProductBatchRepository batches;
  private final QualityInspectionRepository inspections;
  private final InspectionItemResultRepository items;
  private final DefectRecordRepository defects;
  private final BatchConclusionService conclusionService;

  public ProductBatchService(ProductBatchRepository batches, QualityInspectionRepository inspections,
      InspectionItemResultRepository items, DefectRecordRepository defects, BatchConclusionService conclusionService) {
    this.batches = batches;
    this.inspections = inspections;
    this.items = items;
    this.defects = defects;
    this.conclusionService = conclusionService;
  }

  public List<Map<String, Object>> list() {
    return batches.findAll().stream().map(ProductBatchDtoFactory::toResponse).toList();
  }

  public boolean exists(String batchNo) {
    return batches.findByBatchNo(batchNo).isPresent();
  }

  /** Creates a batch; creation is serialized per batch number to avoid duplicates. */
  public Map<String, Object> create(ProductBatchPayload p) {
    if (p == null) {
      throw new ApiException(ErrorCodes.VALIDATION_FAILED, ErrorMessages.VALIDATION_FAILED + "body required", 400);
    }
    PayloadValidator.requireNonBlank("batchNo", p.batchNo());
    return conclusionService.inBatchLock(p.batchNo(), () -> {
      if (batches.findByBatchNo(p.batchNo()).isPresent()) {
        throw new ApiException(ErrorCodes.BATCH_EXISTS, ErrorMessages.BATCH_EXISTS + p.batchNo(), 409);
      }
      ProductBatch b = new ProductBatch();
      b.batchNo = p.batchNo();
      b.workOrderId = p.workOrderId();
      b.quantity = p.quantity();
      b.materialLotNo = p.materialLotNo();
      b.producedAt = Formatters.nowIso();
      b.batchStatus = "CREATED";
      b.conclusion = BatchConclusion.PENDING.name();
      b.conclusionVersion = 0;
      b.conclusionUpdatedAt = Formatters.nowIso();
      batches.save(b);
      log.info(LogTemplates.BATCH_CREATE, b.batchNo);
      return ProductBatchDtoFactory.toResponse(b);
    });
  }

  public Map<String, Object> detail(String batchNo) {
    return ProductBatchDtoFactory.toResponse(findBatch(batchNo));
  }

  /** Retries the conclusion recompute by batch number; the previous conclusion is kept on failure. */
  public Map<String, Object> recompute(String batchNo) {
    return ProductBatchDtoFactory.toResponse(conclusionService.recompute(batchNo));
  }

  /** Full trace tree: the conclusion is always derived from exactly these details. */
  public Map<String, Object> trace(String batchNo) {
    ProductBatch b = findBatch(batchNo);
    String batchId = String.valueOf(b.id);
    List<Map<String, Object>> inspectionDtos = inspections.findByBatchId(batchId).stream()
        .map(q -> QualityInspectionDtoFactory.toResponse(q,
            items.findEffectiveByInspectionId(String.valueOf(q.id)).stream()
                .map(InspectionItemResultDtoFactory::toResponse).toList()))
        .toList();
    List<Map<String, Object>> defectDtos = defects.findByBatchId(batchId).stream()
        .map(DefectRecordDtoFactory::toResponse).toList();
    return ProductBatchDtoFactory.toTrace(b, inspectionDtos, defectDtos);
  }

  private ProductBatch findBatch(String batchNo) {
    return batches.findByBatchNo(batchNo)
        .orElseThrow(() -> new ApiException(ErrorCodes.BATCH_NOT_FOUND,
            ErrorMessages.BATCH_NOT_FOUND + batchNo, 404));
  }
}
