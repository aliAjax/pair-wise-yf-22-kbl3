package com.generated.qualityTrace.constants;

/**
 * 错误消息模板集中定义，与 ErrorCodes 一一对应。
 */
public final class ErrorMessages {

  private ErrorMessages() {}

  public static final String AUTH_REQUIRED = "missing or invalid token";
  public static final String RBAC_DENIED = "role denied: required=%s, actual=%s";
  public static final String VALIDATION_FAILED = "validation failed: %s";
  public static final String NOT_FOUND = "%s not found: %s";
  public static final String CONFLICT = "conflict: %s";
  public static final String RECALCULATION_FAILED = "batch conclusion recalculation failed after %d attempts for batch=%s, previous conclusion preserved";
  public static final String RATE_LIMITED = "rate limit exceeded, retry later";
  public static final String INTERNAL_ERROR = "internal error";

  /** 越权提交专用：质检员不得关闭不良或让步放行 */
  public static final String INSPECTOR_FORBIDDEN_CLOSE = "inspector is not allowed to close defects or grant concession";
}
