package com.tcm.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("paper_question")
public class PaperQuestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long paperId;
    private Long questionId;
    private Integer sort;
    private BigDecimal score;
}
