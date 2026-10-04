package com.generated.qualityTrace.utils;

import java.time.OffsetDateTime;
import com.generated.qualityTrace.constants.BatchConclusion;

public final class Formatters {
  private Formatters() {}

  public static String audit(String type, long id) {
    return type + "#" + id;
  }

  public static String nowIso() {
    return OffsetDateTime.now().toString();
  }

  public static String conclusionText(String conclusion) {
    if (conclusion == null) {
      return "未知";
    }
    if (BatchConclusion.PASS.name().equals(conclusion)) {
      return "合格";
    }
    if (BatchConclusion.FAIL.name().equals(conclusion)) {
      return "不合格";
    }
    if (BatchConclusion.CONDITIONAL_PASS.name().equals(conclusion)) {
      return "让步放行";
    }
    if (BatchConclusion.PENDING.name().equals(conclusion)) {
      return "待判定";
    }
    return "未知";
  }
}
