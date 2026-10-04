package com.generated.qualityTrace.services;

import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import com.generated.qualityTrace.constants.DefectSeverity;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.exceptions.ConflictException;
import com.generated.qualityTrace.exceptions.NotFoundException;
import com.generated.qualityTrace.exceptions.ValidationException;
import com.generated.qualityTrace.middlewares.AuditLogMiddleware;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.repositories.DefectRecordRepository;
import com.generated.qualityTrace.repositories.ProductBatchRepository;
import com.generated.qualityTrace.types.DefectRecordPayload;
import com.generated.qualityTrace.types.LoginUser;
import com.generated.qualityTrace.utils.UserContext;

/**
 * 不良记录服务。质检员登记不良；关闭不良与让步放行归质量经理（控制器层 @RequireRole 拦截）。
 * 登记/关闭后触发批次结论重算（失败按批次号重试，保留上一次结论）。
 */
@Service
public class DefectRecordService {

  private static final Logger log = LoggerFactory.getLogger(DefectRecordService.class);

  private final DefectRecordRepository defectRepo;
  private final ProductBatchRepository batchRepo;
  private final BatchConclusionService conclusionService;
  private final AuditLogMiddleware auditLog;
  private final TransactionTemplate txTemplate;

  public DefectRecordService(DefectRecordRepository defectRepo,
                             ProductBatchRepository batchRepo,
                             BatchConclusionService conclusionService,
                             AuditLogMiddleware auditLog,
                             TransactionTemplate txTemplate) {
    this.defectRepo = defectRepo;
    this.batchRepo = batchRepo;
    this.conclusionService = conclusionService;
    this.auditLog = auditLog;
    this.txTemplate = txTemplate;
  }

  public List<DefectRecord> list() {
    return defectRepo.selectList(null);
  }

  public List<DefectRecord> listByBatch(Long batchId) {
    return defectRepo.findByBatchId(batchId);
  }

  /** 质检员登记不良，触发重算 */
  public Long register(DefectRecordPayload payload) {
    LoginUser actor = UserContext.get();
    if (actor == null) {
      throw new ValidationException("login required");
    }
    validate(payload);
    Long defectId = txTemplate.execute(status -> registerOnce(payload));
    triggerRecalc(payload.batchNo());
    return defectId;
  }

  /** 质量经理关闭不良，触发重算 */
  public void close(Long defectId) {
    LoginUser actor = UserContext.get();
    if (actor == null) {
      throw new ValidationException("login required");
    }
    String batchNo = txTemplate.execute(status -> closeOnce(defectId, actor.userId()));
    triggerRecalc(batchNo);
  }

  /** 质量经理让步放行，触发重算 */
  public void grantConcession(String batchNo) {
    LoginUser actor = UserContext.get();
    if (actor == null) {
      throw new ValidationException("login required");
    }
    txTemplate.executeWithoutResult(status -> grantConcessionOnce(batchNo, actor.userId()));
    triggerRecalc(batchNo);
  }

  private void triggerRecalc(String batchNo) {
    try {
      conclusionService.recalculateWithRetry(batchNo);
    } catch (Exception e) {
      log.error("recalc failed after retries for batch={}, previous conclusion preserved", batchNo, e);
    }
  }

  /** 必须在事务中调用（由 txTemplate 包裹） */
  public Long registerOnce(DefectRecordPayload payload) {
    ProductBatch batch = batchRepo.lockByBatchNo(payload.batchNo());
    if (batch == null) {
      throw new NotFoundException("batch not found: " + payload.batchNo());
    }
    DefectRecord d = new DefectRecord();
    d.setBatchId(batch.getId());
    d.setDefectType(payload.defectType());
    d.setDefectQty(payload.defectQty());
    d.setSeverity(payload.severity());
    d.setRootCause(payload.rootCause());
    d.setDispositionStatus("OPEN");
    d.setRecordedAt(payload.recordedAt() == null ? Instant.now().toString() : payload.recordedAt());
    defectRepo.insert(d);

    auditLog.log("DEFECT_REGISTER", "DEFECT_RECORD", String.valueOf(d.getId()),
        "batch=" + payload.batchNo() + " type=" + payload.defectType() + " severity=" + payload.severity());
    log.info(LogTemplates.DEFECT_REGISTER, payload.batchNo(), d.getId(), payload.defectType(),
        payload.severity(), UserContext.getUsername());
    return d.getId();
  }

  /** 必须在事务中调用（由 txTemplate 包裹） */
  public String closeOnce(Long defectId, Long managerId) {
    DefectRecord d = defectRepo.selectById(defectId);
    if (d == null) {
      throw new NotFoundException("defect not found: " + defectId);
    }
    if ("CLOSED".equals(d.getDispositionStatus())) {
      throw new ConflictException("defect already closed: " + defectId);
    }
    ProductBatch batch = batchRepo.lockById(d.getBatchId());
    if (batch == null) {
      throw new NotFoundException("batch not found: " + d.getBatchId());
    }
    d.setDispositionStatus("CLOSED");
    d.setClosedAt(Instant.now().toString());
    d.setClosedBy(managerId);
    defectRepo.updateById(d);

    auditLog.log("DEFECT_CLOSE", "DEFECT_RECORD", String.valueOf(defectId),
        "batch=" + batch.getBatchNo());
    log.info(LogTemplates.DEFECT_CLOSE, defectId, UserContext.getUsername());
    return batch.getBatchNo();
  }

  /** 必须在事务中调用（由 txTemplate 包裹） */
  public void grantConcessionOnce(String batchNo, Long managerId) {
    ProductBatch batch = batchRepo.lockByBatchNo(batchNo);
    if (batch == null) {
      throw new NotFoundException("batch not found: " + batchNo);
    }
    batch.setConcessionGranted(true);
    batchRepo.updateById(batch);

    auditLog.log("CONCESSION_GRANT", "PRODUCT_BATCH", batchNo, "managerId=" + managerId);
    log.info(LogTemplates.CONCESSION_GRANT, batchNo, UserContext.getUsername());
  }

  private void validate(DefectRecordPayload payload) {
    if (payload == null || payload.batchNo() == null || payload.batchNo().isBlank()) {
      throw new ValidationException("batchNo is required");
    }
    if (payload.defectType() == null || payload.defectType().isBlank()) {
      throw new ValidationException("defectType is required");
    }
    if (payload.severity() == null || payload.severity().isBlank()) {
      throw new ValidationException("severity is required");
    }
    try {
      DefectSeverity.valueOf(payload.severity());
    } catch (IllegalArgumentException e) {
      throw new ValidationException("invalid severity: " + payload.severity());
    }
  }
}
