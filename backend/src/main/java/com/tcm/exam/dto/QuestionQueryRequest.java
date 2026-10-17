package com.tcm.exam.dto;

import lombok.Data;

@Data
public class QuestionQueryRequest {
    private Long subjectId;
    private Long knowledgePointId;
    private String qtype;
    private Integer difficulty;
    private String stem;
    private Integer status;
    private long current = 1;
    private long size = 10;
}
