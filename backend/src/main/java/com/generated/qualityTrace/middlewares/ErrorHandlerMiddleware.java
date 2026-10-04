package com.generated.qualityTrace.middlewares;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.exceptions.ConflictException;
import com.generated.qualityTrace.exceptions.ForbiddenException;
import com.generated.qualityTrace.exceptions.NotFoundException;
import com.generated.qualityTrace.exceptions.RecalculationException;
import com.generated.qualityTrace.exceptions.UnauthorizedException;
import com.generated.qualityTrace.exceptions.ValidationException;

/**
 * 全局异常处理中间件。service 与 controller 分别包装异常后在此统一转成 HTTP 响应，
 * 不吞异常、不把内部错误直接抛给前端。
 */
@RestControllerAdvice
public class ErrorHandlerMiddleware {

  private ResponseEntity<Map<String, Object>> body(HttpStatus status, String code, String message) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("code", code);
    m.put("message", message);
    m.put("status", status.value());
    return ResponseEntity.status(status).body(m);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Map<String, Object>> unauthorized(UnauthorizedException e) {
    return body(HttpStatus.UNAUTHORIZED, ErrorCodes.AUTH_REQUIRED, e.getMessage());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<Map<String, Object>> forbidden(ForbiddenException e) {
    return body(HttpStatus.FORBIDDEN, ErrorCodes.RBAC_DENIED, e.getMessage());
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<Map<String, Object>> validation(ValidationException e) {
    return body(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_FAILED, e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> beanValidation(MethodArgumentNotValidException e) {
    String msg = e.getBindingResult().getFieldErrors().stream()
        .map(f -> f.getField() + " " + f.getDefaultMessage())
        .findFirst().orElse("invalid");
    return body(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_FAILED, msg);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, Object>> notFound(NotFoundException e) {
    return body(HttpStatus.NOT_FOUND, ErrorCodes.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<Map<String, Object>> conflict(ConflictException e) {
    return body(HttpStatus.CONFLICT, ErrorCodes.CONFLICT, e.getMessage());
  }

  @ExceptionHandler(RecalculationException.class)
  public ResponseEntity<Map<String, Object>> recalcFailed(RecalculationException e) {
    // 重算失败时上一次结论已被保留，返回 500 并明确告知未覆盖
    return body(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.RECALCULATION_FAILED, e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> internal(Exception e) {
    return body(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.INTERNAL_ERROR,
        ErrorMessages.INTERNAL_ERROR + ": " + e.getMessage());
  }
}
