package com.generated.qualityTrace.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.exceptions.ConflictException;
import com.generated.qualityTrace.exceptions.NotFoundException;
import com.generated.qualityTrace.exceptions.ValidationException;
import com.generated.qualityTrace.middlewares.AuditLogMiddleware;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.models.WorkOrder;
import com.generated.qualityTrace.repositories.DefectRecordRepository;
import com.generated.qualityTrace.repositories.InspectionItemResultRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.repositories.WorkOrderRepository;
import com.generated.qualityTrace.types.ProductBatchPayload;
import com.generated.qualityTrace.utils.UserContext;

/**
 * 产品批次服务。批次创建后结论由重算维护；追溯查询返回与明细一致的结论。
 */
@Service
public class ProductBatchService {

  private static final Logger log = LoggerFactory.getLogger(ProductBatchService.class);

  private final ProductBatchRepository batchRepo;
  private final WorkOrderRepository workOrderRepo;
  private final InspectionItemResultRepository itemRepo;
  private final DefectRecordRepository defectRepo;
  private final AuditLogMiddleware auditLog;

  public ProductBatchService(ProductBatchRepository batchRepo,
                             WorkOrderRepository workOrderRepo,
                             InspectionItemResultRepository itemRepo,
                             DefectRecordRepository defectRepo,
                             AuditLogMiddleware auditLog) {
    this.batchRepo = batchRepo;
    this.workOrderRepo = workOrderRepo;
    this.itemRepo = itemRepo;
    this.defectRepo = defectRepo;
    this.auditLog = auditLog;
  }

  public List<ProductBatch> list() {
    return batchRepo.selectList(null);
  }

  public ProductBatch getByBatchNo(String batchNo) {
    ProductBatch b = batchRepo.selectByBatchNo(batchNo);
    if (b == null) {
      throw new NotFoundException("batch not found: " + batchNo);
    }
    return b;
  }

  /** 创建批次，初始结论 PASS（后续由重算维护） */
  @Transactional
  public ProductBatch create(ProductBatchPayload payload) {
    if (payload == null || payload.batchNo() == null || payload.batchNo().isBlank()) {
      throw new ValidationException("batchNo is required");
    }
    if (payload.workOrderNo() == null || payload.workOrderNo().isBlank()) {
      throw new ValidationException("workOrderNo is required");
    }
    WorkOrder wo = workOrderRepo.selectByOrderNo(payload.workOrderNo());
    if (wo == null) {
      throw new NotFoundException("work order not found: " + payload.workOrderNo());
    }
    ProductBatch existing = batchRepo.selectByBatchNo(payload.batchNo());
    if (existing != null) {
      throw new ConflictException("batch already exists: " + payload.batchNo());
    }
    ProductBatch b = new ProductBatch();
    b.setBatchNo(payload.batchNo());
    b.setWorkOrderId(wo.getId());
    b.setQuantity(payload.quantity());
    b.setMaterialLotNo(payload.materialLotNo());
    b.setProducedAt(payload.producedAt() == null ? Instant.now().toString() : payload.producedAt());
    b.setBatchStatus("IN_PROGRESS");
    b.setConclusionStatus(com.generated.qualityTrace.constants.InspectionResultStatus.PASS);
    b.setConcessionGranted(false);
    b.setVersion(0);
    batchRepo.insert(b);

    auditLog.log("BATCH_CREATE", "PRODUCT_BATCH", payload.batchNo(),
        "workOrder=" + payload.workOrderNo());
    log.info(LogTemplates.BATCH_CREATE, payload.batchNo(), payload.workOrderNo(),
        UserContext.getUsername());
    return b;
  }

  /**
   * 追溯查询：返回批次 + 已生效检验项 + 不良记录。
   * 结论由已生效检验项与不良记录重算得出，与明细一致（非拼接）。
   */
  public Map<String, Object> trace(String batchNo) {
    ProductBatch b = getByBatchNo(batchNo);
    List<InspectionItemResult> items = itemRepo.findEffectiveByBatchId(b.getId());
    List<DefectRecord> defects = defectRepo.findByBatchId(b.getId());

    Map<String, Object> m = new LinkedHashMap<>();
    m.put("batchNo", b.getBatchNo());
    m.put("batchStatus", b.getBatchStatus());
    m.put("conclusionStatus", b.getConclusionStatus());
    m.put("conclusionUpdatedAt", b.getConclusionUpdatedAt());
    m.put("concessionGranted", b.getConcessionGranted());
    m.put("effectiveItems", items);
    m.put("defects", defects);
    m.put("itemCount", items.size());
    m.put("defectCount", defects.size());

    auditLog.log("BATCH_TRACE", "PRODUCT_BATCH", batchNo,
        "items=" + items.size() + " defects=" + defects.size());
    log.info(LogTemplates.BATCH_TRACE, batchNo, UserContext.getUsername());
    return m;
  }
}
