package com.generated.qualityTrace.types;

/** 检验项录入请求（随检验单一并提交） */
public record InspectionItemPayload(
    String itemCode,
    String itemName,
    String measuredValue,
    String limitMin,
    String limitMax,
    String itemStatus
) {
}
