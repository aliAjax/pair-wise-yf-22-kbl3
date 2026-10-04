package com.generated.qualityTrace.constants;

/**
 * 日志模板集中定义。所有写操作都要记录日志，字段变更时同步改调用处。
 * 每个实体至少 4 条模板。
 */
public final class LogTemplates {

  private LogTemplates() {}

  // ---- 通用动作 ----
  public static final String CREATE = "create";
  public static final String UPDATE = "update";
  public static final String STATUS = "status";
  public static final String EXPORT = "export";

  // ---- 工单 WorkOrder ----
  public static final String WO_CREATE = "work_order.create order_no=%s actor=%s";
  public static final String WO_STATUS = "work_order.status order_no=%s %s -> %s actor=%s";
  public static final String WO_UPDATE = "work_order.update order_no=%s actor=%s";
  public static final String WO_EXPORT = "work_order.export order_no=%s actor=%s";

  // ---- 批次 ProductBatch ----
  public static final String BATCH_CREATE = "batch.create batch_no=%s work_order=%s actor=%s";
  public static final String BATCH_STATUS = "batch.status batch_no=%s %s -> %s actor=%s";
  public static final String BATCH_TRACE = "batch.trace batch_no=%s actor=%s";
  public static final String BATCH_CONCLUSION = "batch.conclusion batch_no=%s conclusion=%s items=%d defects=%d actor=%s";

  // ---- 质量检验 QualityInspection ----
  public static final String QI_SUBMIT = "inspection.submit batch_no=%s inspection_id=%d type=%s actor=%s";
  public static final String QI_RESULT = "inspection.result inspection_id=%d result=%s actor=%s";
  public static final String QI_RECHECK = "inspection.recheck inspection_id=%d actor=%s";
  public static final String QI_EXPORT = "inspection.export inspection_id=%d actor=%s";

  // ---- 检验项结果 InspectionItemResult ----
  public static final String ITEM_SUBMIT = "item.submit batch_no=%s item_code=%s version=%d actor=%s";
  public static final String ITEM_SUPERSEDE = "item.supersede batch_no=%s item_code=%s old_version=%d actor=%s";
  public static final String ITEM_JUDGE = "item.judge item_code=%s status=%s actor=%s";
  public static final String ITEM_EXPORT = "item.export item_code=%s actor=%s";

  // ---- 不良记录 DefectRecord ----
  public static final String DEFECT_REGISTER = "defect.register batch_no=%s defect_id=%d type=%s severity=%s actor=%s";
  public static final String DEFECT_CLOSE = "defect.close defect_id=%d actor=%s";
  public static final String DEFECT_REOPEN = "defect.reopen defect_id=%d actor=%s";
  public static final String DEFECT_EXPORT = "defect.export defect_id=%d actor=%s";

  // ---- 让步放行 Concession ----
  public static final String CONCESSION_GRANT = "concession.grant batch_no=%s actor=%s";
  public static final String CONCESSION_REVOKE = "concession.revoke batch_no=%s actor=%s";

  // ---- 重算 Recalculation ----
  public static final String RECALC_START = "recalc.start batch_no=%s attempt=%d";
  public static final String RECALC_SUCCESS = "recalc.success batch_no=%s conclusion=%s attempt=%d";
  public static final String RECALC_FAIL = "recalc.fail batch_no=%s attempt=%d error=%s";
  public static final String RECALC_RETRY = "recalc.retry batch_no=%s attempt=%d backoff_ms=%d";

  // ---- 认证 Auth ----
  public static final String AUTH_LOGIN = "auth.login username=%s role=%s";
  public static final String AUTH_DENIED = "auth.denied username=%s role=%s required=%s";
}
