package com.tcm.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam")
public class Exam {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long paperId;
    private Long clazzId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMin;
    private BigDecimal passScore;
    private Boolean shuffle;
    private Boolean antiCheat;
    private Integer switchLimit;
    private Integer status;     // 0未开始/1进行中/2已结束/3草稿
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
