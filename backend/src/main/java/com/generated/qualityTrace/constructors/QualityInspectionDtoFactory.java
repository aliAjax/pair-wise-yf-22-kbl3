package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import com.generated.qualityTrace.models.QualityInspection;

/** 检验单响应对象构造器 */
public final class QualityInspectionDtoFactory {

  private QualityInspectionDtoFactory() {}

  public static Map<String, Object> create(QualityInspection q) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", q.getId());
    m.put("batchId", q.getBatchId());
    m.put("inspectorId", q.getInspectorId());
    m.put("inspectionType", q.getInspectionType());
    m.put("standardVersion", q.getStandardVersion());
    m.put("resultStatus", q.getResultStatus());
    m.put("inspectedAt", q.getInspectedAt());
    return m;
  }
}
