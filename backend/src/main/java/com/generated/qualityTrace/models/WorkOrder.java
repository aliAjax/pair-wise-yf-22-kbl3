package com.generated.qualityTrace.models;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 生产工单。批次由工单生成。
 */
@TableName("work_order")
public class WorkOrder {

  @TableId(type = IdType.AUTO)
  private Long id;

  @TableField("order_no")
  private String orderNo;

  @TableField("product_code")
  private String productCode;

  @TableField("product_name")
  private String productName;

  @TableField("planned_qty")
  private Integer plannedQty;

  @TableField("line_code")
  private String lineCode;

  @TableField("start_at")
  private String startAt;

  private String status;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getOrderNo() { return orderNo; }
  public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
  public String getProductCode() { return productCode; }
  public void setProductCode(String productCode) { this.productCode = productCode; }
  public String getProductName() { return productName; }
  public void setProductName(String productName) { this.productName = productName; }
  public Integer getPlannedQty() { return plannedQty; }
  public void setPlannedQty(Integer plannedQty) { this.plannedQty = plannedQty; }
  public String getLineCode() { return lineCode; }
  public void setLineCode(String lineCode) { this.lineCode = lineCode; }
  public String getStartAt() { return startAt; }
  public void setStartAt(String startAt) { this.startAt = startAt; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
