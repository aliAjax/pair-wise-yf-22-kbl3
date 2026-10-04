package com.generated.qualityTrace.constants;

/**
 * 错误码集中定义。service 与 controller 分别包装异常时引用同一套码。
 */
public final class ErrorCodes {

  private ErrorCodes() {}

  /** 未登录 / 令牌缺失或失效 */
  public static final String AUTH_REQUIRED = "AUTH_REQUIRED";
  /** 已登录但角色不足，越权提交被拒绝 */
  public static final String RBAC_DENIED = "RBAC_DENIED";
  /** 入参校验失败 */
  public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
  /** 资源不存在 */
  public static final String NOT_FOUND = "NOT_FOUND";
  /** 资源状态冲突（如重复关闭） */
  public static final String CONFLICT = "CONFLICT";
  /** 批次结论重算失败（重试耗尽，保留上一次结论） */
  public static final String RECALCULATION_FAILED = "RECALCULATION_FAILED";
  /** 触发频率超限 */
  public static final String RATE_LIMITED = "RATE_LIMITED";
  /** 服务内部错误 */
  public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
}
