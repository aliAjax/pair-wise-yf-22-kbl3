package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.constants.Role;
import com.generated.qualityTrace.middlewares.RbacMiddleware;
import com.generated.qualityTrace.routes.InspectionItemResultRoutes;
import com.generated.qualityTrace.services.InspectionItemResultService;
import com.generated.qualityTrace.types.InspectionItemResultPayload;

@RestController
@RequestMapping(InspectionItemResultRoutes.PATH)
public class InspectionItemResultController {
  private final InspectionItemResultService service;
  private final RbacMiddleware rbac;

  public InspectionItemResultController(InspectionItemResultService service, RbacMiddleware rbac) {
    this.service = service;
    this.rbac = rbac;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list();
  }

  /**
   * Inspectors submit or update one item result. A repeated submission of the same
   * item keeps only the latest one and recomputes the batch conclusion.
   */
  @PostMapping
  public Map<String, Object> submit(@RequestParam String inspectionId,
      @RequestBody InspectionItemResultPayload payload, HttpServletRequest request) {
    rbac.require(request, Role.INSPECTOR, Role.QUALITY_MANAGER);
    return service.submit(inspectionId, payload);
  }
}
