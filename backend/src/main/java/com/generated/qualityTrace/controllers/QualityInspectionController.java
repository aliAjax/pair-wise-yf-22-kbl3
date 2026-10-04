package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.constructors.QualityInspectionDtoFactory;
import com.generated.qualityTrace.middlewares.RequireRole;
import com.generated.qualityTrace.models.QualityInspection;
import com.generated.qualityTrace.services.QualityInspectionService;
import com.generated.qualityTrace.types.QualityInspectionPayload;

/**
 * 质量检验控制器。质检员提交检验与检验项（质量经理可代录）。
 */
@RestController
@RequestMapping("/api")
public class QualityInspectionController {

  private final QualityInspectionService service;

  public QualityInspectionController(QualityInspectionService service) {
    this.service = service;
  }

  @PostMapping("/inspections")
  @RequireRole({"INSPECTOR", "MANAGER"})
  public Map<String, Object> submit(@RequestBody QualityInspectionPayload payload) {
    Long id = service.submit(payload);
    return Map.of("inspectionId", id, "batchNo", payload.batchNo());
  }

  @GetMapping("/inspections")
  @RequireRole({"INSPECTOR", "MANAGER", "SUPERVISOR", "AUDITOR"})
  public List<Map<String, Object>> list() {
    return service.list().stream().map(QualityInspectionDtoFactory::create).toList();
  }
}
