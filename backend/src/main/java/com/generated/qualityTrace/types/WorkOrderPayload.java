package com.generated.qualityTrace.types;

/** 工单创建请求 */
public record WorkOrderPayload(
    String orderNo,
    String productCode,
    String productName,
    Integer plannedQty,
    String lineCode,
    String startAt
) {
}
