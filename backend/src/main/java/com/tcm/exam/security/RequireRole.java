package com.tcm.exam.security;

import java.lang.annotation.*;

/**
 * 角色权限注解（RBAC）
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    String[] value();
}
