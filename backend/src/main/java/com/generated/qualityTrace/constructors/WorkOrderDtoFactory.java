package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import com.generated.qualityTrace.models.WorkOrder;

/** 工单响应对象构造器 */
public final class WorkOrderDtoFactory {

  private WorkOrderDtoFactory() {}

  public static Map<String, Object> create(WorkOrder w) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", w.getId());
    m.put("orderNo", w.getOrderNo());
    m.put("productCode", w.getProductCode());
    m.put("productName", w.getProductName());
    m.put("plannedQty", w.getPlannedQty());
    m.put("lineCode", w.getLineCode());
    m.put("startAt", w.getStartAt());
    m.put("status", w.getStatus());
    return m;
  }
}
