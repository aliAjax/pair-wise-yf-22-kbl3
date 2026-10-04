package com.generated.qualityTrace.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.InspectionResultStatus;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constructors.InspectionItemResultDtoFactory;
import com.generated.qualityTrace.constructors.ProductBatchDtoFactory;
import com.generated.qualityTrace.constructors.QualityInspectionDtoFactory;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.repositories.InspectionItemResultRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.repositories.QualityInspectionRepository;
import com.generated.qualityTrace.types.ApiException;
import com.generated.qualityTrace.types.InspectionItemResultPayload;
import com.generated.qualityTrace.types.QualityInspectionPayload;
import com.generated.qualityTrace.utils.Formatters;
import com.generated.qualityTrace.validators.PayloadValidator;

@Service
public class QualityInspectionService {
  private static final Logger log = LoggerFactory.getLogger(QualityInspectionService.class);

  private final QualityInspectionRepository inspections;
  private final InspectionItemResultRepository items;
  private final ProductBatchRepository batches;
  private final BatchConclusionService conclusionService;

  public QualityInspectionService(QualityInspectionRepository inspections, InspectionItemResultRepository items,
      ProductBatchRepository batches, BatchConclusionService conclusionService) {
    this.inspections = inspections;
    this.items = items;
    this.batches = batches;
    this.conclusionService = conclusionService;
  }

  public List<Map<String, Object>> list() {
    return inspections.findAll().stream()
        .map(q -> QualityInspectionDtoFactory.toResponse(q, List.of()))
        .toList();
  }

  /** Submits an inspection with its items, then recomputes the batch conclusion. */
  public Map<String, Object> submit(QualityInspectionPayload p) {
    validate(p);
    return conclusionService.inBatchLock(p.batchNo(), () -> {
      ProductBatch batch = batches.findByBatchNo(p.batchNo())
          .orElseThrow(() -> new ApiException(ErrorCodes.BATCH_NOT_FOUND,
              ErrorMessages.BATCH_NOT_FOUND + p.batchNo(), 404));
      QualityInspection q = new QualityInspection();
      q.batchId = String.valueOf(batch.id);
      q.inspectorId = p.inspectorId();
      q.inspectionType = p.inspectionType();
      q.standardVersion = p.standardVersion();
      q.inspectedAt = Formatters.nowIso();
      inspections.save(q);
      for (InspectionItemResultPayload ip : p.items()) {
        upsertItem(String.valueOf(q.id), ip);
      }
      q.resultStatus = deriveInspectionStatus(String.valueOf(q.id));
      inspections.save(q);
      ProductBatch rebased = conclusionService.recomputeLocked(p.batchNo());
      log.info(LogTemplates.INSPECTION_SUBMIT, p.batchNo(), p.inspectorId(), p.items().size());
      Map<String, Object> resp = new LinkedHashMap<>();
      resp.put("inspection", QualityInspectionDtoFactory.toResponse(q, itemDtos(String.valueOf(q.id))));
      resp.put("batch", ProductBatchDtoFactory.toResponse(rebased));
      return resp;
    });
  }

  /** Upserts one item result; a repeated (inspectionId, itemCode) keeps only the latest submission. */
  InspectionItemResult upsertItem(String inspectionId, InspectionItemResultPayload ip) {
    InspectionItemResult r = new InspectionItemResult();
    r.inspectionId = inspectionId;
    r.itemCode = ip.itemCode();
    r.itemName = ip.itemName();
    r.measuredValue = ip.measuredValue();
    r.limitMin = ip.limitMin();
    r.limitMax = ip.limitMax();
    r.itemStatus = ip.itemStatus();
    r.submittedAt = Formatters.nowIso();
    InspectionItemResult previous = items.upsert(r);
    if (previous == null) {
      log.info(LogTemplates.ITEM_UPSERT, r.inspectionId, r.itemCode);
    } else {
      log.info(LogTemplates.ITEM_DEDUP_REPLACE, r.inspectionId, r.itemCode);
    }
    return r;
  }

  /** Derives the inspection result status from its effective item results. */
  public String deriveInspectionStatus(String inspectionId) {
    List<InspectionItemResult> effective = items.findEffectiveByInspectionId(inspectionId);
    if (effective.stream().anyMatch(i -> InspectionResultStatus.FAIL.name().equals(i.itemStatus))) {
      return InspectionResultStatus.FAIL.name();
    }
    if (effective.stream().anyMatch(i -> InspectionResultStatus.RECHECK.name().equals(i.itemStatus))) {
      return InspectionResultStatus.RECHECK.name();
    }
    return InspectionResultStatus.PASS.name();
  }

  List<Map<String, Object>> itemDtos(String inspectionId) {
    List<Map<String, Object>> dtos = new ArrayList<>();
    for (InspectionItemResult r : items.findEffectiveByInspectionId(inspectionId)) {
      dtos.add(InspectionItemResultDtoFactory.toResponse(r));
    }
    return dtos;
  }

  private void validate(QualityInspectionPayload p) {
    if (p == null) {
      throw new ApiException(ErrorCodes.VALIDATION_FAILED, ErrorMessages.VALIDATION_FAILED + "body required", 400);
    }
    PayloadValidator.requireNonBlank("batchNo", p.batchNo());
    PayloadValidator.requireNonBlank("inspectorId", p.inspectorId());
    if (p.items() == null || p.items().isEmpty()) {
      throw new ApiException(ErrorCodes.VALIDATION_FAILED, ErrorMessages.VALIDATION_FAILED + "items required", 400);
    }
    for (InspectionItemResultPayload ip : p.items()) {
      PayloadValidator.requireNonBlank("itemCode", ip.itemCode());
      PayloadValidator.requireEnum(InspectionResultStatus.class, "itemStatus", ip.itemStatus());
    }
  }
}
