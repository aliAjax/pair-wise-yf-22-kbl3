package com.generated.qualityTrace.types;

/** 不良登记请求 */
public record DefectRecordPayload(
    String batchNo,
    String defectType,
    Integer defectQty,
    String severity,
    String rootCause,
    String recordedAt
) {
}
