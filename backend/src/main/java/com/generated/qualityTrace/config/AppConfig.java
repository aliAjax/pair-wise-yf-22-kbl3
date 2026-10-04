package com.generated.qualityTrace.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.generated.qualityTrace.middlewares.AuditLogMiddleware;
import com.generated.qualityTrace.middlewares.AuthMiddleware;
import com.generated.qualityTrace.middlewares.RateLimitMiddleware;
import com.generated.qualityTrace.services.ProductBatchService;
import com.generated.qualityTrace.types.ProductBatchPayload;

@Configuration
public class AppConfig implements WebMvcConfigurer {
  private final AuthMiddleware authMiddleware;
  private final RateLimitMiddleware rateLimitMiddleware;
  private final AuditLogMiddleware auditLogMiddleware;

  public AppConfig(AuthMiddleware authMiddleware, RateLimitMiddleware rateLimitMiddleware,
      AuditLogMiddleware auditLogMiddleware) {
    this.authMiddleware = authMiddleware;
    this.rateLimitMiddleware = rateLimitMiddleware;
    this.auditLogMiddleware = auditLogMiddleware;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rateLimitMiddleware).addPathPatterns("/api/**");
    registry.addInterceptor(authMiddleware).addPathPatterns("/api/**");
    registry.addInterceptor(auditLogMiddleware).addPathPatterns("/api/**");
  }

  @Bean
  CommandLineRunner seedDemoBatch(ProductBatchService productBatchService) {
    return args -> {
      if (!productBatchService.exists("BATCH-2026-001")) {
        productBatchService.create(new ProductBatchPayload("BATCH-2026-001", "WO-1", "1000", "MAT-001"));
      }
    };
  }
}
