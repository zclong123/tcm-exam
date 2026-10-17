package com.tcm.exam.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScoreVO {
    private Long scoreId;
    private Long examId;
    private String examName;
    private Long studentId;
    private String studentName;
    private String clazzName;
    private BigDecimal totalScore;
    private Boolean isPass;
    private Integer rankNo;
    private LocalDateTime createdAt;
}
