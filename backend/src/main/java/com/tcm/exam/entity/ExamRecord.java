package com.tcm.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_record")
public class ExamRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long studentId;
    private String paperSnapshot;   // JSON: 乱序后的题目列表
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private BigDecimal totalScore;
    private String status;          // ING/SUBMITTED/AUTO/GRADED
    private Integer switchCount;
    private String ip;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
