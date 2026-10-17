package com.tcm.exam.vo;

import com.tcm.exam.dto.QuestionDTO;
import com.tcm.exam.entity.Exam;
import com.tcm.exam.entity.ExamAnswer;
import com.tcm.exam.entity.ExamRecord;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ExamDetailVO {
    private Exam exam;
    private ExamRecord record;
    private List<QuestionDTO> questions;          // 乱序后的题目（不含答案）
    private Map<Long, ExamAnswer> answers;        // 已作答（断点续答）
    private Integer durationMin;
    private Boolean shuffle;
}
