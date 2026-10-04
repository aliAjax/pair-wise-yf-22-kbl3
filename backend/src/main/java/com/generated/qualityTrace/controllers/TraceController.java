package com.generated.qualityTrace.controllers;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.generated.qualityTrace.routes.TraceRoutes;
import com.generated.qualityTrace.services.ProductBatchService;

@RestController
public class TraceController {
  private final ProductBatchService service;

  public TraceController(ProductBatchService service) {
    this.service = service;
  }

  /** Full trace tree for a batch: conclusion plus the details it was derived from. */
  @GetMapping(TraceRoutes.PATH + "/{batchNo}")
  public Map<String, Object> trace(@PathVariable String batchNo) {
    return service.trace(batchNo);
  }
}
