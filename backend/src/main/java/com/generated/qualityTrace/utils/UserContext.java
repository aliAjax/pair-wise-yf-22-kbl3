package com.generated.qualityTrace.utils;

import com.generated.qualityTrace.types.LoginUser;

/**
 * 登录用户 ThreadLocal。认证中间件写入，业务层读取角色做越权校验，请求结束清理。
 */
public final class UserContext {

  private static final ThreadLocal<LoginUser> CURRENT = new ThreadLocal<>();

  private UserContext() {}

  public static void set(LoginUser user) { CURRENT.set(user); }

  public static LoginUser get() { return CURRENT.get(); }

  public static Long getUserId() {
    LoginUser u = CURRENT.get();
    return u == null ? null : u.userId();
  }

  public static String getUsername() {
    LoginUser u = CURRENT.get();
    return u == null ? null : u.username();
  }

  public static String getRole() {
    LoginUser u = CURRENT.get();
    return u == null ? null : u.role();
  }

  public static void clear() { CURRENT.remove(); }
}
