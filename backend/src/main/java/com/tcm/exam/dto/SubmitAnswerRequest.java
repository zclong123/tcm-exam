package com.tcm.exam.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmitAnswerRequest {
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        private Long questionId;
        private String answer;    // 客观题: "A"/"AB"/"对"/"错"/文本; 主观题: 作答文本
        private Boolean isMarked;
    }
}
