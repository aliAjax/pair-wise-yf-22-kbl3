package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.constructors.WorkOrderDtoFactory;
import com.generated.qualityTrace.middlewares.RequireRole;
import com.generated.qualityTrace.models.WorkOrder;
import com.generated.qualityTrace.services.WorkOrderService;
import com.generated.qualityTrace.types.WorkOrderPayload;

/**
 * 工单控制器。创建工单归产线主管/质量经理。
 */
@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

  private final WorkOrderService service;

  public WorkOrderController(WorkOrderService service) {
    this.service = service;
  }

  @PostMapping
  @RequireRole({"SUPERVISOR", "MANAGER"})
  public Map<String, Object> create(@RequestBody WorkOrderPayload payload) {
    WorkOrder w = service.create(payload);
    return WorkOrderDtoFactory.create(w);
  }

  @GetMapping
  @RequireRole({"INSPECTOR", "MANAGER", "SUPERVISOR", "AUDITOR"})
  public List<Map<String, Object>> list() {
    return service.list().stream().map(WorkOrderDtoFactory::create).toList();
  }
}
