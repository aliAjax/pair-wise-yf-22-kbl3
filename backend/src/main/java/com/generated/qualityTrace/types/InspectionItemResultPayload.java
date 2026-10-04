package com.generated.qualityTrace.types;

/** 检验项结果查询/响应对象 */
public record InspectionItemResultPayload(
    Long id,
    Long inspectionId,
    Long batchId,
    String itemCode,
    String itemName,
    String measuredValue,
    String limitMin,
    String limitMax,
    String itemStatus,
    Integer version,
    Boolean effective,
    String submittedAt
) {
}
