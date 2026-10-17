package com.tcm.exam.service;

import com.tcm.exam.common.PageResult;
import com.tcm.exam.dto.SmartPaperRequest;
import com.tcm.exam.entity.Paper;
import com.tcm.exam.entity.PaperQuestion;
import com.tcm.exam.entity.Question;

import java.util.List;

public interface PaperService {
    /**
     * 按规则抽题（不落库），供智能组卷与学生随机组卷复用
     */
    List<Question> draftQuestions(SmartPaperRequest req);

    /**
     * 智能组卷：按规则抽题并生成试卷
     */
    Paper generateSmart(SmartPaperRequest req);

    /**
     * 手工组卷：选择题目入卷
     */
    Paper createManual(String name, Long subjectId, List<Long> questionIds);

    PageResult<Paper> page(int current, int size, String name);

    Paper getById(Long id);

    List<PaperQuestion> listPaperQuestions(Long paperId);

    void delete(Long id);
}
