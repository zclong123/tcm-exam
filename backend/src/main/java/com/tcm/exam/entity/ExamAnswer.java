package com.tcm.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_answer")
public class ExamAnswer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private Long questionId;
    private String studentAnswer;
    private Boolean isMarked;
    private Boolean isCorrect;
    private BigDecimal score;
    private String reviewStatus;    // NONE/AUTO/REVIEWED
    private String reviewComment;
    private Long reviewedBy;
    private LocalDateTime reviewedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
