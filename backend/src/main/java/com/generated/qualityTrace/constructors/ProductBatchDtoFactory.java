package com.generated.qualityTrace.constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import com.generated.qualityTrace.models.ProductBatch;

/** 批次响应对象构造器：页面/服务不得直接散写默认结构 */
public final class ProductBatchDtoFactory {

  private ProductBatchDtoFactory() {}

  public static Map<String, Object> create(ProductBatch b) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", b.getId());
    m.put("batchNo", b.getBatchNo());
    m.put("workOrderId", b.getWorkOrderId());
    m.put("quantity", b.getQuantity());
    m.put("materialLotNo", b.getMaterialLotNo());
    m.put("producedAt", b.getProducedAt());
    m.put("batchStatus", b.getBatchStatus());
    m.put("conclusionStatus", b.getConclusionStatus());
    m.put("conclusionUpdatedAt", b.getConclusionUpdatedAt());
    m.put("concessionGranted", b.getConcessionGranted());
    m.put("version", b.getVersion());
    return m;
  }
}
