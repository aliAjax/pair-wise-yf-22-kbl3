package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.constructors.InspectionItemResultDtoFactory;
import com.generated.qualityTrace.middlewares.RequireRole;
import com.generated.qualityTrace.models.InspectionItemResult;
import com.generated.qualityTrace.services.InspectionItemResultService;

/**
 * 检验项结果查询控制器。
 */
@RestController
@RequestMapping("/api/inspection-items")
public class InspectionItemResultController {

  private final InspectionItemResultService service;

  public InspectionItemResultController(InspectionItemResultService service) {
    this.service = service;
  }

  @GetMapping
  @RequireRole({"INSPECTOR", "MANAGER", "SUPERVISOR", "AUDITOR"})
  public List<Map<String, Object>> list(@RequestParam(required = false) String batchNo) {
    List<InspectionItemResult> rows = (batchNo == null || batchNo.isBlank())
        ? service.list()
        : service.listEffectiveByBatch(batchNo);
    return rows.stream().map(InspectionItemResultDtoFactory::create).toList();
  }
}
