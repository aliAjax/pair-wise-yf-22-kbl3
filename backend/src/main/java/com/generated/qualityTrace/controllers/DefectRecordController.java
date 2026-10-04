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
import com.generated.qualityTrace.middlewares.AuthMiddleware;
import com.generated.qualityTrace.middlewares.RbacMiddleware;
import com.generated.qualityTrace.routes.DefectRecordRoutes;
import com.generated.qualityTrace.services.DefectRecordService;
import com.generated.qualityTrace.types.DefectRecordPayload;

@RestController
@RequestMapping(DefectRecordRoutes.PATH)
public class DefectRecordController {
  private final DefectRecordService service;
  private final RbacMiddleware rbac;

  public DefectRecordController(DefectRecordService service, RbacMiddleware rbac) {
    this.service = service;
    this.rbac = rbac;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list();
  }

  /** Inspectors register a defect; the batch conclusion is recomputed. */
  @PostMapping
  public Map<String, Object> register(@RequestBody DefectRecordPayload payload, HttpServletRequest request) {
    rbac.require(request, Role.INSPECTOR, Role.QUALITY_MANAGER);
    return service.register(payload);
  }

  /** Closing a defect is restricted to the quality manager. */
  @PostMapping(DefectRecordRoutes.CLOSE)
  public Map<String, Object> close(@PathVariable Long id, HttpServletRequest request) {
    rbac.require(request, Role.QUALITY_MANAGER);
    return service.close(id, actor(request));
  }

  /** Concession release is restricted to the quality manager. */
  @PostMapping(DefectRecordRoutes.CONCESSION)
  public Map<String, Object> concession(@PathVariable Long id, HttpServletRequest request) {
    rbac.require(request, Role.QUALITY_MANAGER);
    return service.concession(id, actor(request));
  }

  private String actor(HttpServletRequest request) {
    return String.valueOf(request.getAttribute(AuthMiddleware.USER_ATTR));
  }
}
