package com.generated.qualityTrace.middlewares;

import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.generated.qualityTrace.constants.ErrorCodes;
import com.generated.qualityTrace.constants.ErrorMessages;
import com.generated.qualityTrace.exceptions.ForbiddenException;
import com.generated.qualityTrace.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * RBAC 中间件：读取方法/类上的 @RequireRole，校验当前用户角色。
 * 质检员录检验/不良；关闭不良与让步放行归质量经理；越权提交拒绝。
 */
@Component
public class RbacMiddleware implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    if (!(handler instanceof HandlerMethod hm)) {
      return true;
    }
    RequireRole methodAnno = hm.getMethodAnnotation(RequireRole.class);
    RequireRole typeAnno = hm.getBeanType().getAnnotation(RequireRole.class);
    RequireRole anno = methodAnno != null ? methodAnno : typeAnno;
    if (anno == null) {
      return true;
    }
    String role = UserContext.getRole();
    if (role == null) {
      throw new ForbiddenException(ErrorMessages.RBAC_DENIED.formatted(
          Arrays.toString(anno.value()), "anonymous"));
    }
    for (String allowed : anno.value()) {
      if (allowed.equals(role)) {
        return true;
      }
    }
    throw new ForbiddenException(ErrorMessages.RBAC_DENIED.formatted(
        Arrays.toString(anno.value()), role));
  }
}
