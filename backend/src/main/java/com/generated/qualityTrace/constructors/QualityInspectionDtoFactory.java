package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.generated.qualityTrace.models.QualityInspection;

public final class QualityInspectionDtoFactory {
  private QualityInspectionDtoFactory() {}

  public static Map<String, Object> toResponse(QualityInspection q, List<Map<String, Object>> items) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", q.id);
    m.put("batchId", q.batchId);
    m.put("inspectorId", q.inspectorId);
    m.put("inspectionType", q.inspectionType);
    m.put("standardVersion", q.standardVersion);
    m.put("resultStatus", q.resultStatus);
    m.put("inspectedAt", q.inspectedAt);
    m.put("items", items);
    return m;
  }
}
