package com.tcm.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_point")
public class KnowledgePoint {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long subjectId;
    private String name;
    private Long parentId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
