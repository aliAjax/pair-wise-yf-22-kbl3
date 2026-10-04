package com.generated.qualityTrace.models;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 质量检验单。一次检验包含多个检验项结果，提交后触发批次结论重算。
 */
@TableName("quality_inspection")
public class QualityInspection {

  @TableId(type = IdType.AUTO)
  private Long id;

  @TableField("batch_id")
  private Long batchId;

  @TableField("inspector_id")
  private Long inspectorId;

  @TableField("inspection_type")
  private String inspectionType;

  @TableField("standard_version")
  private String standardVersion;

  @TableField("result_status")
  private String resultStatus;

  @TableField("inspected_at")
  private String inspectedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getBatchId() { return batchId; }
  public void setBatchId(Long batchId) { this.batchId = batchId; }
  public Long getInspectorId() { return inspectorId; }
  public void setInspectorId(Long inspectorId) { this.inspectorId = inspectorId; }
  public String getInspectionType() { return inspectionType; }
  public void setInspectionType(String inspectionType) { this.inspectionType = inspectionType; }
  public String getStandardVersion() { return standardVersion; }
  public void setStandardVersion(String standardVersion) { this.standardVersion = standardVersion; }
  public String getResultStatus() { return resultStatus; }
  public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
  public String getInspectedAt() { return inspectedAt; }
  public void setInspectedAt(String inspectedAt) { this.inspectedAt = inspectedAt; }
}
