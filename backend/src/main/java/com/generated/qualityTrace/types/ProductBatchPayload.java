package com.generated.qualityTrace.types;

/** 批次创建请求 */
public record ProductBatchPayload(
    String batchNo,
    String workOrderNo,
    Integer quantity,
    String materialLotNo,
    String producedAt
) {
}
