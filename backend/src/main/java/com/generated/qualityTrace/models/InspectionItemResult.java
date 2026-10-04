package com.generated.qualityTrace.models;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 检验项结果。同一 (batchId, itemCode) 重复提交只留最新一条 effective=true，
 * 历史记录置 effective=false，批次重算只读已生效记录。
 */
@TableName("inspection_item_result")
public class InspectionItemResult {

  @TableId(type = IdType.AUTO)
  private Long id;

  @TableField("inspection_id")
  private Long inspectionId;

  @TableField("batch_id")
  private Long batchId;

  @TableField("item_code")
  private String itemCode;

  @TableField("item_name")
  private String itemName;

  @TableField("measured_value")
  private String measuredValue;

  @TableField("limit_min")
  private String limitMin;

  @TableField("limit_max")
  private String limitMax;

  @TableField("item_status")
  private String itemStatus;

  private Integer version;

  /** 最新一次提交为 true，历史提交为 false */
  private Boolean effective;

  @TableField("submitted_at")
  private String submittedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getInspectionId() { return inspectionId; }
  public void setInspectionId(Long inspectionId) { this.inspectionId = inspectionId; }
  public Long getBatchId() { return batchId; }
  public void setBatchId(Long batchId) { this.batchId = batchId; }
  public String getItemCode() { return itemCode; }
  public void setItemCode(String itemCode) { this.itemCode = itemCode; }
  public String getItemName() { return itemName; }
  public void setItemName(String itemName) { this.itemName = itemName; }
  public String getMeasuredValue() { return measuredValue; }
  public void setMeasuredValue(String measuredValue) { this.measuredValue = measuredValue; }
  public String getLimitMin() { return limitMin; }
  public void setLimitMin(String limitMin) { this.limitMin = limitMin; }
  public String getLimitMax() { return limitMax; }
  public void setLimitMax(String limitMax) { this.limitMax = limitMax; }
  public String getItemStatus() { return itemStatus; }
  public void setItemStatus(String itemStatus) { this.itemStatus = itemStatus; }
  public Integer getVersion() { return version; }
  public void setVersion(Integer version) { this.version = version; }
  public Boolean getEffective() { return effective; }
  public void setEffective(Boolean effective) { this.effective = effective; }
  public String getSubmittedAt() { return submittedAt; }
  public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
}
