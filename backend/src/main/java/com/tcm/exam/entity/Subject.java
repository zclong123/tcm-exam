package com.tcm.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("subject")
public class Subject {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String name;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
