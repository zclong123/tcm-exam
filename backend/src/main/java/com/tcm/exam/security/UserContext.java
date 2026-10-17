package com.tcm.exam.security;

import com.tcm.exam.common.BizException;

/**
 * 当前登录用户上下文（ThreadLocal）
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();
    private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

    public static void set(Long userId, String username, String role) {
        USER_ID.set(userId);
        USERNAME.set(username);
        ROLE.set(role);
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
        ROLE.remove();
    }

    public static Long userId() {
        Long id = USER_ID.get();
        if (id == null) {
            throw new BizException(401, "未登录或登录已过期");
        }
        return id;
    }

    public static String username() {
        return USERNAME.get();
    }

    public static String role() {
        return ROLE.get();
    }
}
