package com.tcm.exam.service;

import com.tcm.exam.common.PageResult;
import com.tcm.exam.dto.SubmitAnswerRequest;
import com.tcm.exam.entity.Exam;
import com.tcm.exam.entity.ExamAnswer;
import com.tcm.exam.entity.ExamRecord;
import com.tcm.exam.vo.ExamDetailVO;

import java.util.List;

public interface ExamService {
    Long create(Exam exam);

    void update(Exam exam);

    void delete(Long id);

    PageResult<Exam> page(int current, int size, String name, Long clazzId);

    List<Exam> studentExams();

    ExamDetailVO startExam(Long examId);

    void saveAnswer(Long recordId, SubmitAnswerRequest req);

    void submit(Long recordId);

    void reportSwitch(Long recordId);

    void autoSubmitOverdue();

    ExamRecord getRecord(Long recordId);
}
