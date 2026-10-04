package com.generated.qualityTrace.models;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/** 操作日志实体 */
@TableName("audit_log")
public class AuditLog {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String actor;

  private String action;

  @TableField("target_type")
  private String targetType;

  @TableField("target_id")
  private String targetId;

  private String detail;

  @TableField("created_at")
  private String createdAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getActor() { return actor; }
  public void setActor(String actor) { this.actor = actor; }
  public String getAction() { return action; }
  public void setAction(String action) { this.action = action; }
  public String getTargetType() { return targetType; }
  public void setTargetType(String targetType) { this.targetType = targetType; }
  public String getTargetId() { return targetId; }
  public void setTargetId(String targetId) { this.targetId = targetId; }
  public String getDetail() { return detail; }
  public void setDetail(String detail) { this.detail = detail; }
  public String getCreatedAt() { return createdAt; }
  public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
