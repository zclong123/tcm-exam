package com.tcm.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("question_answer")
public class QuestionAnswer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private String answerType;
    private String answerText;
}
