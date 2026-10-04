package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import com.generated.qualityTrace.models.DefectRecord;

/** 不良记录响应对象构造器 */
public final class DefectRecordDtoFactory {

  private DefectRecordDtoFactory() {}

  public static Map<String, Object> create(DefectRecord d) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", d.getId());
    m.put("batchId", d.getBatchId());
    m.put("defectType", d.getDefectType());
    m.put("defectQty", d.getDefectQty());
    m.put("severity", d.getSeverity());
    m.put("rootCause", d.getRootCause());
    m.put("dispositionStatus", d.getDispositionStatus());
    m.put("recordedAt", d.getRecordedAt());
    m.put("closedAt", d.getClosedAt());
    m.put("closedBy", d.getClosedBy());
    return m;
  }
}
