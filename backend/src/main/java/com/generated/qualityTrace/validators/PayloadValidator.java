package com.generated.qualityTrace.validators;

import java.util.Arrays;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.types.ApiException;

public final class PayloadValidator {
  private PayloadValidator() {}

  public static void requireNonBlank(String field, String value) {
    if (value == null || value.isBlank()) {
      throw new ApiException(ErrorCodes.VALIDATION_FAILED, ErrorMessages.VALIDATION_FAILED + field + " required", 400);
    }
  }

  public static <E extends Enum<E>> void requireEnum(Class<E> type, String field, String value) {
    requireNonBlank(field, value);
    try {
      Enum.valueOf(type, value);
    } catch (IllegalArgumentException e) {
      throw new ApiException(ErrorCodes.VALIDATION_FAILED,
          ErrorMessages.VALIDATION_FAILED + field + " must be one of " + Arrays.toString(type.getEnumConstants()), 400);
    }
  }

  public static long requireNumericId(String field, String value) {
    requireNonBlank(field, value);
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException e) {
      throw new ApiException(ErrorCodes.VALIDATION_FAILED, ErrorMessages.VALIDATION_FAILED + field + " must be numeric", 400);
    }
  }
}
