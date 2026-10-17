package com.tcm.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("question_stat")
public class QuestionStat {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private Integer answerCount;
    private Integer correctCount;
    private BigDecimal correctRate;
    private BigDecimal difficultyReal;
    private BigDecimal discrimination;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
