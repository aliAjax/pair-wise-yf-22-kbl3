package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.constants.Role;
import com.generated.qualityTrace.middlewares.RbacMiddleware;
import com.generated.qualityTrace.routes.ProductBatchRoutes;
import com.generated.qualityTrace.services.ProductBatchService;
import com.generated.qualityTrace.types.ProductBatchPayload;

@RestController
@RequestMapping(ProductBatchRoutes.PATH)
public class ProductBatchController {
  private final ProductBatchService service;
  private final RbacMiddleware rbac;

  public ProductBatchController(ProductBatchService service, RbacMiddleware rbac) {
    this.service = service;
    this.rbac = rbac;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list();
  }

  @GetMapping(ProductBatchRoutes.DETAIL)
  public Map<String, Object> detail(@PathVariable String batchNo) {
    return service.detail(batchNo);
  }

  @PostMapping
  public Map<String, Object> create(@RequestBody ProductBatchPayload payload, HttpServletRequest request) {
    rbac.require(request, Role.INSPECTOR, Role.LINE_SUPERVISOR, Role.QUALITY_MANAGER);
    return service.create(payload);
  }

  /** Retries the batch conclusion recompute by batch number after a failure. */
  @PostMapping(ProductBatchRoutes.RECOMPUTE)
  public Map<String, Object> recompute(@PathVariable String batchNo, HttpServletRequest request) {
    rbac.require(request, Role.INSPECTOR, Role.QUALITY_MANAGER);
    return service.recompute(batchNo);
  }
}
