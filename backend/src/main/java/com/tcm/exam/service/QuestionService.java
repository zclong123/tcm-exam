package com.tcm.exam.service;

import com.tcm.exam.common.PageResult;
import com.tcm.exam.dto.QuestionDTO;
import com.tcm.exam.dto.QuestionQueryRequest;

import java.util.List;

public interface QuestionService {
    PageResult<QuestionDTO> page(QuestionQueryRequest req);

    QuestionDTO getById(Long id);

    Long create(QuestionDTO dto);

    void update(QuestionDTO dto);

    void delete(Long id);

    void audit(Long id, Integer status, String remark);

    /**
     * 组卷时获取题目完整信息（含选项/答案/解析）
     */
    List<QuestionDTO> listByIds(List<Long> ids);

    /**
     * 按学科/知识点统计题目数
     */
    List<QuestionDTO> countBySubject();
}
