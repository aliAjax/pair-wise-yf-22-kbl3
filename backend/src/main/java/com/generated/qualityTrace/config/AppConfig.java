package com.generated.qualityTrace.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.generated.qualityTrace.middlewares.RbacMiddleware;

/**
 * 应用配置：注册 RBAC 拦截器。认证/限流中间件以 Filter 形式自动注册。
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {

  private final RbacMiddleware rbacMiddleware;

  public AppConfig(RbacMiddleware rbacMiddleware) {
    this.rbacMiddleware = rbacMiddleware;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rbacMiddleware).addPathPatterns("/api/**");
  }
}
