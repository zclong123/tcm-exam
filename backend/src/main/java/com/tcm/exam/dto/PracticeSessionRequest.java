package com.tcm.exam.dto;

import lombok.Data;

/**
 * 练习会话请求：章节练习 / 随机练习
 */
@Data
public class PracticeSessionRequest {
    private String mode;          // CHAPTER | RANDOM
    private Long subjectId;
    private Long knowledgePointId;
    private String qtype;         // 可选题型过滤，空则混合
    private Integer count;        // 题量
}
