package com.tcm.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("score")
public class Score {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long studentId;
    private Long recordId;
    private BigDecimal totalScore;
    private Boolean isPass;
    private Integer rankNo;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
