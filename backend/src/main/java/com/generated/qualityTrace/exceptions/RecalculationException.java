package com.generated.qualityTrace.exceptions;

/**
 * 批次结论重算失败。重试耗尽后抛出，此时上一次结论被保留（未被覆盖）。
 */
public class RecalculationException extends RuntimeException {
  public RecalculationException(String message) { super(message); }
  public RecalculationException(String message, Throwable cause) { super(message, cause); }
}
