package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.constants.Role;
import com.generated.qualityTrace.middlewares.RbacMiddleware;
import com.generated.qualityTrace.routes.QualityInspectionRoutes;
import com.generated.qualityTrace.services.QualityInspectionService;
import com.generated.qualityTrace.types.QualityInspectionPayload;

@RestController
@RequestMapping(QualityInspectionRoutes.PATH)
public class QualityInspectionController {
  private final QualityInspectionService service;
  private final RbacMiddleware rbac;

  public QualityInspectionController(QualityInspectionService service, RbacMiddleware rbac) {
    this.service = service;
    this.rbac = rbac;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list();
  }

  /** Inspectors submit an inspection with its item results; the batch conclusion is recomputed. */
  @PostMapping
  public Map<String, Object> submit(@RequestBody QualityInspectionPayload payload, HttpServletRequest request) {
    rbac.require(request, Role.INSPECTOR, Role.QUALITY_MANAGER);
    return service.submit(payload);
  }
}
