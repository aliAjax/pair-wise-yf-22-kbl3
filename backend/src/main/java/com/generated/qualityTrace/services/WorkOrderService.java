package com.generated.qualityTrace.services;

import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constants.WorkOrderStatus;
import com.generated.qualityTrace.exceptions.ConflictException;
import com.generated.qualityTrace.exceptions.ValidationException;
import com.generated.qualityTrace.middlewares.AuditLogMiddleware;
import com.generated.qualityTrace.models.WorkOrder;
import com.generated.qualityTrace.repositories.WorkOrderRepository;
import com.generated.qualityTrace.types.WorkOrderPayload;
import com.generated.qualityTrace.utils.UserContext;

/**
 * 生产工单服务。
 */
@Service
public class WorkOrderService {

  private static final Logger log = LoggerFactory.getLogger(WorkOrderService.class);

  private final WorkOrderRepository repo;
  private final AuditLogMiddleware auditLog;

  public WorkOrderService(WorkOrderRepository repo, AuditLogMiddleware auditLog) {
    this.repo = repo;
    this.auditLog = auditLog;
  }

  public List<WorkOrder> list() {
    return repo.selectList(null);
  }

  @Transactional
  public WorkOrder create(WorkOrderPayload payload) {
    if (payload == null || payload.orderNo() == null || payload.orderNo().isBlank()) {
      throw new ValidationException("orderNo is required");
    }
    if (payload.productCode() == null || payload.productCode().isBlank()) {
      throw new ValidationException("productCode is required");
    }
    if (repo.selectByOrderNo(payload.orderNo()) != null) {
      throw new ConflictException("work order already exists: " + payload.orderNo());
    }
    WorkOrder w = new WorkOrder();
    w.setOrderNo(payload.orderNo());
    w.setProductCode(payload.productCode());
    w.setProductName(payload.productName());
    w.setPlannedQty(payload.plannedQty());
    w.setLineCode(payload.lineCode());
    w.setStartAt(payload.startAt() == null ? Instant.now().toString() : payload.startAt());
    w.setStatus(WorkOrderStatus.PLANNED);
    repo.insert(w);

    auditLog.log("WO_CREATE", "WORK_ORDER", payload.orderNo(),
        "productCode=" + payload.productCode());
    log.info(LogTemplates.WO_CREATE, payload.orderNo(), UserContext.getUsername());
    return w;
  }
}
