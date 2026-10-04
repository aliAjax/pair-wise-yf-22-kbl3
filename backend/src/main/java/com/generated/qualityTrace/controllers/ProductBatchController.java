package com.generated.qualityTrace.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.constructors.ProductBatchDtoFactory;
import com.generated.qualityTrace.middlewares.RequireRole;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.services.ProductBatchService;
import com.generated.qualityTrace.types.ProductBatchPayload;

/**
 * 产品批次控制器。创建批次归产线主管/质量经理；追溯查询对所有登录角色开放。
 */
@RestController
@RequestMapping("/api")
public class ProductBatchController {

  private final ProductBatchService service;

  public ProductBatchController(ProductBatchService service) {
    this.service = service;
  }

  @PostMapping("/batches")
  @RequireRole({"SUPERVISOR", "MANAGER"})
  public Map<String, Object> create(@RequestBody ProductBatchPayload payload) {
    ProductBatch b = service.create(payload);
    return ProductBatchDtoFactory.create(b);
  }

  @GetMapping("/batches")
  @RequireRole({"INSPECTOR", "MANAGER", "SUPERVISOR", "AUDITOR"})
  public List<Map<String, Object>> list() {
    return service.list().stream().map(ProductBatchDtoFactory::create).toList();
  }

  /** 追溯查询：结论由已生效检验项与不良记录重算得出，与明细一致 */
  @GetMapping("/trace/{batchNo}")
  @RequireRole({"INSPECTOR", "MANAGER", "SUPERVISOR", "AUDITOR"})
  public Map<String, Object> trace(@PathVariable String batchNo) {
    return service.trace(batchNo);
  }
}
