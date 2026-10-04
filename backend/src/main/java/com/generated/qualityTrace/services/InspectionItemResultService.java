package com.generated.qualityTrace.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.InspectionResultStatus;
import com.generated.qualityTrace.constructors.InspectionItemResultDtoFactory;
import com.generated.qualityTrace.constructors.ProductBatchDtoFactory;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.repositories.InspectionItemResultRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.repositories.QualityInspectionRepository;
import com.generated.qualityTrace.types.ApiException;
import com.generated.qualityTrace.types.InspectionItemResultPayload;
import com.generated.qualityTrace.validators.PayloadValidator;

@Service
public class InspectionItemResultService {
  private final InspectionItemResultRepository items;
  private final QualityInspectionRepository inspections;
  private final ProductBatchRepository batches;
  private final QualityInspectionService inspectionService;
  private final BatchConclusionService conclusionService;

  public InspectionItemResultService(InspectionItemResultRepository items, QualityInspectionRepository inspections,
      ProductBatchRepository batches, QualityInspectionService inspectionService, BatchConclusionService conclusionService) {
    this.items = items;
    this.inspections = inspections;
    this.batches = batches;
    this.inspectionService = inspectionService;
    this.conclusionService = conclusionService;
  }

  public List<Map<String, Object>> list() {
    return items.findAll().stream().map(InspectionItemResultDtoFactory::toResponse).toList();
  }

  /**
   * Submits or updates one item result. A repeated submission of the same item keeps
   * only the latest one, and every update triggers a recompute of the old conclusion.
   */
  public Map<String, Object> submit(String inspectionId, InspectionItemResultPayload p) {
    if (p == null) {
      throw new ApiException(ErrorCodes.VALIDATION_FAILED, ErrorMessages.VALIDATION_FAILED + "body required", 400);
    }
    PayloadValidator.requireNonBlank("itemCode", p.itemCode());
    PayloadValidator.requireEnum(InspectionResultStatus.class, "itemStatus", p.itemStatus());
    QualityInspection q = inspections.findById(PayloadValidator.requireNumericId("inspectionId", inspectionId))
        .orElseThrow(() -> new ApiException(ErrorCodes.INSPECTION_NOT_FOUND,
            ErrorMessages.INSPECTION_NOT_FOUND + inspectionId, 404));
    ProductBatch batch = batches.findById(PayloadValidator.requireNumericId("batchId", q.batchId))
        .orElseThrow(() -> new ApiException(ErrorCodes.BATCH_NOT_FOUND,
            ErrorMessages.BATCH_NOT_FOUND + q.batchId, 404));
    return conclusionService.inBatchLock(batch.batchNo, () -> {
      InspectionItemResult r = inspectionService.upsertItem(String.valueOf(q.id), p);
      q.resultStatus = inspectionService.deriveInspectionStatus(String.valueOf(q.id));
      inspections.save(q);
      ProductBatch rebased = conclusionService.recomputeLocked(batch.batchNo);
      Map<String, Object> resp = new LinkedHashMap<>();
      resp.put("item", InspectionItemResultDtoFactory.toResponse(r));
      resp.put("batch", ProductBatchDtoFactory.toResponse(rebased));
      return resp;
    });
  }
}
