package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import com.generated.qualityTrace.models.InspectionItemResult;

public final class InspectionItemResultDtoFactory {
  private InspectionItemResultDtoFactory() {}

  public static Map<String, Object> toResponse(InspectionItemResult r) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", r.id);
    m.put("inspectionId", r.inspectionId);
    m.put("itemCode", r.itemCode);
    m.put("itemName", r.itemName);
    m.put("measuredValue", r.measuredValue);
    m.put("limitMin", r.limitMin);
    m.put("limitMax", r.limitMax);
    m.put("itemStatus", r.itemStatus);
    m.put("submittedAt", r.submittedAt);
    return m;
  }
}
