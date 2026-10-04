package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.constructors.DefectRecordDtoFactory;
import com.generated.qualityTrace.middlewares.RequireRole;
import com.generated.qualityTrace.models.DefectRecord;
import com.generated.qualityTrace.services.DefectRecordService;
import com.generated.qualityTrace.types.DefectRecordPayload;

/**
 * 不良记录控制器。
 * 登记不良归质检员；关闭不良与让步放行归质量经理，越权提交拒绝。
 */
@RestController
@RequestMapping("/api")
public class DefectRecordController {

  private final DefectRecordService service;

  public DefectRecordController(DefectRecordService service) {
    this.service = service;
  }

  /** 质检员登记不良 */
  @PostMapping("/defects")
  @RequireRole({"INSPECTOR", "MANAGER"})
  public Map<String, Object> register(@RequestBody DefectRecordPayload payload) {
    Long id = service.register(payload);
    return Map.of("defectId", id, "batchNo", payload.batchNo());
  }

  /** 质量经理关闭不良 */
  @PostMapping("/defects/{id}/close")
  @RequireRole({"MANAGER"})
  public Map<String, Object> close(@PathVariable Long id) {
    service.close(id);
    return Map.of("defectId", id, "dispositionStatus", "CLOSED");
  }

  /** 质量经理让步放行 */
  @PostMapping("/batches/{batchNo}/concession")
  @RequireRole({"MANAGER"})
  public Map<String, Object> concession(@PathVariable String batchNo) {
    service.grantConcession(batchNo);
    return Map.of("batchNo", batchNo, "concessionGranted", true);
  }

  @GetMapping("/defects")
  @RequireRole({"INSPECTOR", "MANAGER", "SUPERVISOR", "AUDITOR"})
  public List<Map<String, Object>> list() {
    return service.list().stream().map(DefectRecordDtoFactory::create).toList();
  }
}
