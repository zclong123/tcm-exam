package com.tcm.exam.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 题目完整 DTO（题干+选项+答案+解析+知识点）
 */
@Data
public class QuestionDTO {
    private Long id;
    private Long subjectId;
    private String subjectName;
    private Long knowledgePointId;
    private String knowledgePointName;
    private String qtype;
    private Integer difficulty;
    private String cognitiveLevel;
    private String stem;
    private BigDecimal score;
    private Integer status;
    private List<OptionDTO> options;
    private List<AnswerDTO> answers;
    private String analysis;
}
