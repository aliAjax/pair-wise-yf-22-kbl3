package com.generated.qualityTrace.models;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 系统用户。角色：INSPECTOR / MANAGER / SUPERVISOR / AUDITOR。
 */
@TableName("sys_user")
public class SysUser {

  @TableId(type = IdType.AUTO)
  private Long id;

  private String username;

  private String password;

  private String role;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }
}
