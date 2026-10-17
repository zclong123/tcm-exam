package com.tcm.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("question_analysis")
public class QuestionAnalysis {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private String analysisText;
}
