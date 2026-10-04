package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import com.generated.qualityTrace.models.InspectionItemResult;

/** 检验项结果响应对象构造器 */
public final class InspectionItemResultDtoFactory {

  private InspectionItemResultDtoFactory() {}

  public static Map<String, Object> create(InspectionItemResult it) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", it.getId());
    m.put("inspectionId", it.getInspectionId());
    m.put("batchId", it.getBatchId());
    m.put("itemCode", it.getItemCode());
    m.put("itemName", it.getItemName());
    m.put("measuredValue", it.getMeasuredValue());
    m.put("limitMin", it.getLimitMin());
    m.put("limitMax", it.getLimitMax());
    m.put("itemStatus", it.getItemStatus());
    m.put("version", it.getVersion());
    m.put("effective", it.getEffective());
    m.put("submittedAt", it.getSubmittedAt());
    return m;
  }
}
