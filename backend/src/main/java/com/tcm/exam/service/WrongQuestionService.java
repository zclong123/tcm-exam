package com.tcm.exam.service;

import com.tcm.exam.common.PageResult;
import com.tcm.exam.dto.QuestionDTO;
import com.tcm.exam.entity.WrongQuestion;

public interface WrongQuestionService {
    PageResult<QuestionDTO> page(int current, int size, Long knowledgePointId, Boolean mastered);

    void markMastered(Long wrongId);

    void redo(Long wrongId);
}
