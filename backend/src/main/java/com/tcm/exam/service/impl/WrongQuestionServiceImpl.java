package com.tcm.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcm.exam.common.BizException;
import com.tcm.exam.common.PageResult;
import com.tcm.exam.dto.QuestionDTO;
import com.tcm.exam.entity.WrongQuestion;
import com.tcm.exam.mapper.WrongQuestionMapper;
import com.tcm.exam.security.UserContext;
import com.tcm.exam.service.QuestionService;
import com.tcm.exam.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WrongQuestionServiceImpl implements WrongQuestionService {

    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionService questionService;

    @Override
    public PageResult<QuestionDTO> page(int current, int size, Long knowledgePointId, Boolean mastered) {
        Long studentId = UserContext.userId();
        LambdaQueryWrapper<WrongQuestion> qw = new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getStudentId, studentId);
        if (mastered != null) qw.eq(WrongQuestion::getMastered, mastered);
        qw.orderByDesc(WrongQuestion::getUpdatedAt);
        Page<WrongQuestion> page = wrongQuestionMapper.selectPage(new Page<>(current, size), qw);

        List<Long> qids = page.getRecords().stream().map(WrongQuestion::getQuestionId)
                .collect(Collectors.toList());
        List<QuestionDTO> questions = questionService.listByIds(qids);
        return PageResult.of(page.getTotal(), questions);
    }

    @Override
    public void markMastered(Long wrongId) {
        WrongQuestion wq = getOwn(wrongId);
        wq.setMastered(true);
        wrongQuestionMapper.updateById(wq);
    }

    @Override
    public void redo(Long wrongId) {
        WrongQuestion wq = getOwn(wrongId);
        wq.setReviewInterval((wq.getReviewInterval() == null ? 1 : wq.getReviewInterval()) * 2);
        wq.setLastReviewAt(LocalDateTime.now());
        wq.setMastered(false);
        wrongQuestionMapper.updateById(wq);
    }

    private WrongQuestion getOwn(Long wrongId) {
        WrongQuestion wq = wrongQuestionMapper.selectById(wrongId);
        if (wq == null) throw new BizException(400, "错题记录不存在");
        if (!wq.getStudentId().equals(UserContext.userId())) {
            throw new BizException(403, "无权访问");
        }
        return wq;
    }
}
