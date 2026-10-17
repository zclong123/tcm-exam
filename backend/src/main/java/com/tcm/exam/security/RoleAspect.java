package com.tcm.exam.security;

import com.tcm.exam.common.BizException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * RBAC 注解切面：校验当前用户角色
 */
@Aspect
@Component
public class RoleAspect {

    @Before("@annotation(requireRole)")
    public void check(RequireRole requireRole) {
        String role = UserContext.role();
        boolean pass = Arrays.asList(requireRole.value()).contains(role);
        if (!pass) {
            throw new BizException(403, "无权限访问，需要角色: " + String.join("/", requireRole.value()));
        }
    }
}
