package com.generated.qualityTrace.models;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 产品批次。批次结论由已生效检验项与不良记录重算得出，不接受逐次拼接。
 */
@TableName("product_batch")
public class ProductBatch {

  @TableId(type = IdType.AUTO)
  private Long id;

  @TableField("batch_no")
  private String batchNo;

  @TableField("work_order_id")
  private Long workOrderId;

  private Integer quantity;

  @TableField("material_lot_no")
  private String materialLotNo;

  @TableField("produced_at")
  private String producedAt;

  @TableField("batch_status")
  private String batchStatus;

  /** 批次结论：PASS / FAIL / CONDITIONAL_PASS，由重算维护 */
  @TableField("conclusion_status")
  private String conclusionStatus;

  @TableField("conclusion_updated_at")
  private String conclusionUpdatedAt;

  /** 质量经理让步放行标记 */
  @TableField("concession_granted")
  private Boolean concessionGranted;

  private Integer version;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getBatchNo() { return batchNo; }
  public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
  public Long getWorkOrderId() { return workOrderId; }
  public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
  public Integer getQuantity() { return quantity; }
  public void setQuantity(Integer quantity) { this.quantity = quantity; }
  public String getMaterialLotNo() { return materialLotNo; }
  public void setMaterialLotNo(String materialLotNo) { this.materialLotNo = materialLotNo; }
  public String getProducedAt() { return producedAt; }
  public void setProducedAt(String producedAt) { this.producedAt = producedAt; }
  public String getBatchStatus() { return batchStatus; }
  public void setBatchStatus(String batchStatus) { this.batchStatus = batchStatus; }
  public String getConclusionStatus() { return conclusionStatus; }
  public void setConclusionStatus(String conclusionStatus) { this.conclusionStatus = conclusionStatus; }
  public String getConclusionUpdatedAt() { return conclusionUpdatedAt; }
  public void setConclusionUpdatedAt(String conclusionUpdatedAt) { this.conclusionUpdatedAt = conclusionUpdatedAt; }
  public Boolean getConcessionGranted() { return concessionGranted; }
  public void setConcessionGranted(Boolean concessionGranted) { this.concessionGranted = concessionGranted; }
  public Integer getVersion() { return version; }
  public void setVersion(Integer version) { this.version = version; }
}
