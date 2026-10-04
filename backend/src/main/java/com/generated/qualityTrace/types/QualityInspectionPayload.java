package com.generated.qualityTrace.types;

import java.util.List;

public record QualityInspectionPayload(String batchNo, String inspectorId, String inspectionType,
    String standardVersion, List<InspectionItemResultPayload> items) {}
