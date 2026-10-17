package com.tcm.exam.dto;

import lombok.Data;

import java.util.List;

/**
 * 智能组卷请求
 */
@Data
public class SmartPaperRequest {
    private String name;
    private Long subjectId;
    private Integer difficulty;           // 整体难度倾向
    private List<TypeRule> typeRules;     // 各题型数量与分值
    private List<Long> knowledgePoints;   // 必考知识点（可空）
    private Boolean ensureRandom = true;  // 尽量随机

    @Data
    public static class TypeRule {
        private String qtype;      // A/X/判断/填空/B
        private Integer count;     // 抽取数量
        private Double score;      // 每题分值
        private Integer difficulty; // 该题型的难度（可空，默认取整体）
    }
}
