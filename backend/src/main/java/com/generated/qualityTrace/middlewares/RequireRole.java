package com.generated.qualityTrace.middlewares;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色校验注解。标注在控制器方法或类上，由 RbacInterceptor 拦截校验。
 * 质检员录检验/不良；关闭不良与让步放行归质量经理；越权提交拒绝。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
  /** 允许访问的角色，如 {"INSPECTOR","MANAGER"} */
  String[] value();
}
