package com.tcm.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("paper")
public class Paper {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String paperType;   // SMART/MANUAL
    private BigDecimal totalScore;
    private Integer questionCount;
    private Long subjectId;
    private Integer difficulty;
    private String ruleJson;
    private Long createdBy;
    private Integer status;     // 0草稿/1已发布
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
