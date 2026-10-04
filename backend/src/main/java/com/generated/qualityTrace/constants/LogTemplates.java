package com.generated.qualityTrace.constants;

public final class LogTemplates {
  public static final String CREATE = "create";
  public static final String UPDATE = "update";
  public static final String STATUS = "status";
  public static final String EXPORT = "export";
  public static final String BATCH_CREATE = "batch create batchNo=%s";
  public static final String INSPECTION_SUBMIT = "inspection submit batchNo=%s inspector=%s items=%d";
  public static final String ITEM_UPSERT = "item upsert inspectionId=%s itemCode=%s";
  public static final String ITEM_DEDUP_REPLACE = "item dedup replace inspectionId=%s itemCode=%s keep latest";
  public static final String DEFECT_REGISTER = "defect register batchNo=%s type=%s severity=%s";
  public static final String DEFECT_CLOSE = "defect close id=%d actor=%s";
  public static final String DEFECT_CONCESSION = "defect concession id=%d actor=%s";
  public static final String CONCLUSION_RECOMPUTE_OK = "conclusion recompute batchNo=%s conclusion=%s version=%d";
  public static final String CONCLUSION_RECOMPUTE_BUSY = "conclusion recompute rejected, another one in effect batchNo=%s";
  public static final String CONCLUSION_RECOMPUTE_FAIL = "conclusion recompute failed batchNo=%s reason=%s previous conclusion kept";
  public static final String RBAC_DENY = "rbac deny role=%s path=%s";
  public static final String AUDIT_WRITE = "audit write method=%s path=%s role=%s status=%d";
  public static final String RATE_LIMIT = "rate limit hit ip=%s";
}
