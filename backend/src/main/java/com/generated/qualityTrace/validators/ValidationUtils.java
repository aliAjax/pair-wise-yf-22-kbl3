package com.generated.qualityTrace.validators;

import com.generated.qualityTrace.exceptions.ValidationException;

/**
 * 入参校验工具。service 层调用，校验失败抛 ValidationException，由全局异常处理转 400。
 */
public final class ValidationUtils {

  private ValidationUtils() {}

  public static void requireNotBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new ValidationException(field + " is required");
    }
  }

  public static void requireNonNull(Object value, String field) {
    if (value == null) {
      throw new ValidationException(field + " is required");
    }
  }

  public static void requirePositive(Integer value, String field) {
    if (value == null || value <= 0) {
      throw new ValidationException(field + " must be positive");
    }
  }
}
