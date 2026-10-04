package com.generated.qualityTrace.middlewares;

import java.io.IOException;
import java.util.Set;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.types.LoginUser;
import com.generated.qualityTrace.utils.JwtUtils;
import com.generated.qualityTrace.utils.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 认证中间件：解析 Authorization: Bearer <token>，写入 UserContext。
 * 白名单路径（健康检查、登录）直接放行；其余路径缺令牌或令牌失效返回 401。
 */
@Component
@Order(1)
public class AuthMiddleware extends OncePerRequestFilter {

  private static final Set<String> WHITELIST = Set.of("/health", "/api/auth/login");

  private final JwtUtils jwtUtils;

  public AuthMiddleware(JwtUtils jwtUtils) {
    this.jwtUtils = jwtUtils;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String path = request.getRequestURI();
    if (WHITELIST.contains(path)) {
      chain.doFilter(request, response);
      return;
    }
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      writeUnauthorized(response, ErrorCodes.AUTH_REQUIRED, "missing token");
      return;
    }
    String token = header.substring(7);
    try {
      LoginUser user = jwtUtils.parse(token);
      UserContext.set(user);
      chain.doFilter(request, response);
    } catch (IllegalArgumentException e) {
      writeUnauthorized(response, ErrorCodes.AUTH_REQUIRED, e.getMessage());
    } finally {
      UserContext.clear();
    }
  }

  private void writeUnauthorized(HttpServletResponse response, String code, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
  }
}
