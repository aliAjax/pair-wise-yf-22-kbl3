package com.generated.qualityTrace.models;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 不良记录。登记不良归质检员，关闭不良归质量经理；处置状态参与批次结论重算。
 */
@TableName("defect_record")
public class DefectRecord {

  @TableId(type = IdType.AUTO)
  private Long id;

  @TableField("batch_id")
  private Long batchId;

  @TableField("defect_type")
  private String defectType;

  @TableField("defect_qty")
  private Integer defectQty;

  private String severity;

  @TableField("root_cause")
  private String rootCause;

  /** OPEN / CLOSED */
  @TableField("disposition_status")
  private String dispositionStatus;

  @TableField("recorded_at")
  private String recordedAt;

  @TableField("closed_at")
  private String closedAt;

  @TableField("closed_by")
  private Long closedBy;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getBatchId() { return batchId; }
  public void setBatchId(Long batchId) { this.batchId = batchId; }
  public String getDefectType() { return defectType; }
  public void setDefectType(String defectType) { this.defectType = defectType; }
  public Integer getDefectQty() { return defectQty; }
  public void setDefectQty(Integer defectQty) { this.defectQty = defectQty; }
  public String getSeverity() { return severity; }
  public void setSeverity(String severity) { this.severity = severity; }
  public String getRootCause() { return rootCause; }
  public void setRootCause(String rootCause) { this.rootCause = rootCause; }
  public String getDispositionStatus() { return dispositionStatus; }
  public void setDispositionStatus(String dispositionStatus) { this.dispositionStatus = dispositionStatus; }
  public String getRecordedAt() { return recordedAt; }
  public void setRecordedAt(String recordedAt) { this.recordedAt = recordedAt; }
  public String getClosedAt() { return closedAt; }
  public void setClosedAt(String closedAt) { this.closedAt = closedAt; }
  public Long getClosedBy() { return closedBy; }
  public void setClosedBy(Long closedBy) { this.closedBy = closedBy; }
}
