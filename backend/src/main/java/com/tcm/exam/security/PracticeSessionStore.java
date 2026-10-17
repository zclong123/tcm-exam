package com.tcm.exam.security;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 练习会话存储（内存，2h 过期）。
 * 用于「章节/随机练习」的归属校验与题目范围校验：
 * 客户端拿到的是净化后的题目，服务端据此限制每名学生只能对会话内题目判分。
 * 后端重启即失效（demo 可接受）。
 */
@Component
public class PracticeSessionStore {

    private static final long TTL_HOURS = 2;
    private final Map<String, Session> store = new ConcurrentHashMap<>();

    public record Session(Long studentId, List<Long> questionIds, LocalDateTime expiresAt) {
    }

    public String create(Long studentId, List<Long> questionIds) {
        String id = UUID.randomUUID().toString().replace("-", "");
        store.put(id, new Session(studentId, questionIds, LocalDateTime.now().plusHours(TTL_HOURS)));
        return id;
    }

    /**
     * 校验归属 + 有效期；无效返回 null
     */
    public Session getOwn(String sessionId, Long studentId) {
        Session s = store.get(sessionId);
        if (s == null) return null;
        if (s.expiresAt().isBefore(LocalDateTime.now())) {
            store.remove(sessionId);
            return null;
        }
        if (!s.studentId().equals(studentId)) return null;
        return s;
    }
}
