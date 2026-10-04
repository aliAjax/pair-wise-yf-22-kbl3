package com.generated.qualityTrace.middlewares;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.generated.qualityTrace.constants.LogTemplates;

/** Writes an audit log line for every non-GET /api/** call. */
@Component
public class AuditLogMiddleware implements HandlerInterceptor {
  private static final Logger log = LoggerFactory.getLogger(AuditLogMiddleware.class);

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {
    if (!"GET".equalsIgnoreCase(request.getMethod())) {
      log.info(LogTemplates.AUDIT_WRITE, request.getMethod(), request.getRequestURI(),
          String.valueOf(request.getAttribute(AuthMiddleware.ROLE_ATTR)), response.getStatus());
    }
  }
}
