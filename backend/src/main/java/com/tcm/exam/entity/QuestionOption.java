package com.tcm.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("question_option")
public class QuestionOption {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private String optionKey;   // A/B/C/D...
    private String optionText;
    private Integer isCorrect;
}
