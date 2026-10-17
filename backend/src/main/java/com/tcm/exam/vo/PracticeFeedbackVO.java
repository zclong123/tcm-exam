package com.tcm.exam.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 练习单题立即反馈：正确与否 + 标准答案 + 解析
 */
@Data
public class PracticeFeedbackVO {
    private Long questionId;
    private Boolean correct;          // 主观题为 null（不自动判分）
    private String correctAnswer;     // 客观题标准答案 / 主观题参考答案
    private String analysis;
    private BigDecimal score;
}
