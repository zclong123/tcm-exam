package com.tcm.exam.vo;

import com.tcm.exam.dto.QuestionDTO;
import com.tcm.exam.entity.ExamRecord;
import lombok.Data;

import java.util.List;

/**
 * 学生随机组卷考试视图（record 用于交卷/判分，questions 为净化后题目）
 */
@Data
public class PracticeExamVO {
    private ExamRecord record;
    private List<QuestionDTO> questions;
    private Integer durationMin;
}
