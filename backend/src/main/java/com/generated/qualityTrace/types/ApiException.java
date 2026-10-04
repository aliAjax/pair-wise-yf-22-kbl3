package com.generated.qualityTrace.types;

public class ApiException extends RuntimeException {
  public final String code;
  public final int status;

  public ApiException(String code, String message, int status) {
    super(message);
    this.code = code;
    this.status = status;
  }
}
