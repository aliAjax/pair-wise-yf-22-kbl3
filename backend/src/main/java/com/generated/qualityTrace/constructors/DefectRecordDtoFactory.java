package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import com.generated.qualityTrace.models.DefectRecord;

public final class DefectRecordDtoFactory {
  private DefectRecordDtoFactory() {}

  public static Map<String, Object> toResponse(DefectRecord d) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", d.id);
    m.put("batchId", d.batchId);
    m.put("defectType", d.defectType);
    m.put("defectQty", d.defectQty);
    m.put("severity", d.severity);
    m.put("rootCause", d.rootCause);
    m.put("dispositionStatus", d.dispositionStatus);
    m.put("createdAt", d.createdAt);
    return m;
  }
}
