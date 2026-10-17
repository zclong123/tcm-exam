package com.tcm.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("wrong_question")
public class WrongQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long questionId;
    private String sourceType;      // EXAM/PRACTICE
    private Long sourceExamId;
    private Integer wrongTimes;
    private Boolean mastered;
    private Integer reviewInterval;
    private LocalDateTime lastReviewAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
