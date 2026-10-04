package com.generated.qualityTrace.types;

import java.util.List;

/** 检验提交请求：一次提交包含多个检验项结果 */
public record QualityInspectionPayload(
    String batchNo,
    String inspectionType,
    String standardVersion,
    String inspectedAt,
    List<InspectionItemPayload> items
) {
}
