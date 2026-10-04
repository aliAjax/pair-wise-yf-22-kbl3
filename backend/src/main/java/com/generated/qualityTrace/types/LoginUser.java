package com.generated.qualityTrace.types;

/** 当前登录用户上下文，由认证中间件写入 ThreadLocal */
public record LoginUser(Long userId, String username, String role) {
}
