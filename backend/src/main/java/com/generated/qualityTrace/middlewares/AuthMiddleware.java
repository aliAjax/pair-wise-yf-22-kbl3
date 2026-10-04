package com.generated.qualityTrace.middlewares;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.constants.Role;
import com.generated.qualityTrace.types.ApiException;

/**
 * Authenticates /api/** requests via the X-Auth-Token and X-Role headers and
 * exposes the resolved role as a request attribute for the RBAC checks.
 */
@Component
public class AuthMiddleware implements HandlerInterceptor {
  public static final String ROLE_ATTR = "role";
  public static final String USER_ATTR = "user";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String token = request.getHeader("X-Auth-Token");
    if (token == null || token.isBlank()) {
      throw new ApiException(ErrorCodes.AUTH_REQUIRED, ErrorMessages.AUTH_REQUIRED, 401);
    }
    String roleHeader = request.getHeader("X-Role");
    Role role;
    try {
      role = Role.valueOf(roleHeader == null ? "" : roleHeader.trim());
    } catch (IllegalArgumentException e) {
      throw new ApiException(ErrorCodes.AUTH_REQUIRED, ErrorMessages.AUTH_REQUIRED, 401);
    }
    request.setAttribute(ROLE_ATTR, role);
    request.setAttribute(USER_ATTR, request.getHeader("X-User-Id"));
    return true;
  }
}
