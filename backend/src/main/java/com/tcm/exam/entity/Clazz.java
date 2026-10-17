package com.tcm.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("clazz")
public class Clazz {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long departmentId;
    private String grade;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
