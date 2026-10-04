package com.generated.qualityTrace.utils;

import com.generated.qualityTrace.constants.DefectSeverity;
import com.generated.qualityTrace.constants.InspectionResultStatus;
import com.generated.qualityTrace.constants.WorkOrderStatus;

/**
 * 格式化工具：状态文案、风险等级、审计标识等混合逻辑，供多个服务/控制器共同依赖。
 */
public final class Formatters {

  private Formatters() {}

  public static String audit(String type, long id) {
    return type + "#" + id;
  }

  /** 检验结论状态文案 */
  public static String inspectionText(String status) {
    if (status == null) {
      return "未知";
    }
    switch (status) {
      case InspectionResultStatus.PASS: return "合格";
      case InspectionResultStatus.FAIL: return "不合格";
      case InspectionResultStatus.CONDITIONAL_PASS: return "有条件放行";
      case InspectionResultStatus.RECHECK: return "复检";
      default: return status;
    }
  }

  /** 批次结论文案（复用检验结论枚举） */
  public static String batchConclusionText(String conclusion) {
    return inspectionText(conclusion);
  }

  /** 不良严重度文案 */
  public static String severityText(String severity) {
    if (severity == null) {
      return "未知";
    }
    switch (severity) {
      case DefectSeverity.MINOR: return "轻微";
      case DefectSeverity.MAJOR: return "主要";
      case DefectSeverity.CRITICAL: return "致命";
      default: return severity;
    }
  }

  /** 工单状态文案 */
  public static String workOrderText(String status) {
    if (status == null) {
      return "未知";
    }
    switch (status) {
      case WorkOrderStatus.PLANNED: return "已计划";
      case WorkOrderStatus.RUNNING: return "生产中";
      case WorkOrderStatus.PAUSED: return "已暂停";
      case WorkOrderStatus.FINISHED: return "已完工";
      case WorkOrderStatus.CANCELLED: return "已取消";
      default: return status;
    }
  }

  /** 处置状态文案 */
  public static String dispositionText(String disposition) {
    if (disposition == null) {
      return "未知";
    }
    switch (disposition) {
      case "OPEN": return "未关闭";
      case "CLOSED": return "已关闭";
      default: return disposition;
    }
  }
}
