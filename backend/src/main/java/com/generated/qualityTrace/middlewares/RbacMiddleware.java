package com.generated.qualityTrace.middlewares;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.LogTemplates;
import com.generated.qualityTrace.constants.Role;
import com.generated.qualityTrace.types.ApiException;

/**
 * Enforces role permissions per endpoint. Inspectors record inspections and
 * defects; closing defects and concession release are restricted to the
 * quality manager. Anything else is rejected with 403 RBAC_DENIED.
 */
@Component
public class RbacMiddleware {
  private static final Logger log = LoggerFactory.getLogger(RbacMiddleware.class);

  public void require(HttpServletRequest request, Role... allowed) {
    Object role = request.getAttribute(AuthMiddleware.ROLE_ATTR);
    for (Role a : allowed) {
      if (a == role) {
        return;
      }
    }
    log.warn(LogTemplates.RBAC_DENY, String.valueOf(role), request.getRequestURI());
    throw new ApiException(ErrorCodes.RBAC_DENIED, ErrorMessages.RBAC_DENIED, 403);
  }
}
