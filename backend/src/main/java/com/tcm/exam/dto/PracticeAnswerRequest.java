package com.tcm.exam.dto;

import lombok.Data;

/**
 * 练习单题作答请求
 */
@Data
public class PracticeAnswerRequest {
    private Long questionId;
    private String answer;
}
