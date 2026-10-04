package com.generated.qualityTrace.middlewares;

import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.types.ApiException;

@RestControllerAdvice
public class ErrorHandlerMiddleware {
  private static final Logger log = LoggerFactory.getLogger(ErrorHandlerMiddleware.class);

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> api(ApiException e) {
    return ResponseEntity.status(e.status).body(body(e.code, String.valueOf(e.getMessage())));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> other(Exception e) {
    log.error("unhandled error: {}", String.valueOf(e.getMessage()));
    return ResponseEntity.status(500).body(body(ErrorCodes.INTERNAL_ERROR, ErrorMessages.INTERNAL_ERROR));
  }

  private Map<String, Object> body(String code, String message) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("code", code);
    m.put("message", message);
    return m;
  }
}
