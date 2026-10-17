package com.tcm.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long subjectId;
    private Long knowledgePointId;
    private String qtype;           // A/X/判断/填空/B/名解/简答/论述/病案
    private Integer difficulty;     // 1-5
    private String cognitiveLevel;  // 记忆/理解/应用/分析
    private String stem;
    private BigDecimal score;
    private Integer status;         // 0待审/1通过/2驳回
    private String auditRemark;
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    @TableField(select = false)
    private LocalDateTime deletedAt;
}
