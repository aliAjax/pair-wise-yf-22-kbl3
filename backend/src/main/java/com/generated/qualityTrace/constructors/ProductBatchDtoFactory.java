package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.generated.qualityTrace.models.ProductBatch;
import com.generated.qualityTrace.utils.Formatters;

public final class ProductBatchDtoFactory {
  private ProductBatchDtoFactory() {}

  public static Map<String, Object> toResponse(ProductBatch b) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", b.id);
    m.put("batchNo", b.batchNo);
    m.put("workOrderId", b.workOrderId);
    m.put("quantity", b.quantity);
    m.put("materialLotNo", b.materialLotNo);
    m.put("producedAt", b.producedAt);
    m.put("batchStatus", b.batchStatus);
    m.put("conclusion", b.conclusion);
    m.put("conclusionText", Formatters.conclusionText(b.conclusion));
    m.put("conclusionVersion", b.conclusionVersion);
    m.put("conclusionUpdatedAt", b.conclusionUpdatedAt);
    m.put("lastRecomputeError", b.lastRecomputeError);
    return m;
  }

  public static Map<String, Object> toTrace(ProductBatch b, List<Map<String, Object>> inspections,
      List<Map<String, Object>> defects) {
    Map<String, Object> m = new LinkedHashMap<>(toResponse(b));
    m.put("inspections", inspections);
    m.put("defects", defects);
    return m;
  }
}
