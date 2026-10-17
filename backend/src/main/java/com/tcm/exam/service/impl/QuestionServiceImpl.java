package com.tcm.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcm.exam.common.BizException;
import com.tcm.exam.common.PageResult;
import com.tcm.exam.dto.AnswerDTO;
import com.tcm.exam.dto.OptionDTO;
import com.tcm.exam.dto.QuestionDTO;
import com.tcm.exam.dto.QuestionQueryRequest;
import com.tcm.exam.entity.*;
import com.tcm.exam.mapper.*;
import com.tcm.exam.security.UserContext;
import com.tcm.exam.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper optionMapper;
    private final QuestionAnswerMapper answerMapper;
    private final QuestionAnalysisMapper analysisMapper;
    private final SubjectMapper subjectMapper;
    private final KnowledgePointMapper kpMapper;

    @Override
    public PageResult<QuestionDTO> page(QuestionQueryRequest req) {
        LambdaQueryWrapper<Question> qw = new LambdaQueryWrapper<>();
        if (req.getSubjectId() != null) qw.eq(Question::getSubjectId, req.getSubjectId());
        if (req.getKnowledgePointId() != null) qw.eq(Question::getKnowledgePointId, req.getKnowledgePointId());
        if (StringUtils.hasText(req.getQtype())) qw.eq(Question::getQtype, req.getQtype());
        if (req.getDifficulty() != null) qw.eq(Question::getDifficulty, req.getDifficulty());
        if (StringUtils.hasText(req.getStem())) qw.like(Question::getStem, req.getStem());
        if (req.getStatus() != null) qw.eq(Question::getStatus, req.getStatus());
        qw.orderByDesc(Question::getId);
        Page<Question> page = questionMapper.selectPage(new Page<>(req.getCurrent(), req.getSize()), qw);
        List<QuestionDTO> list = page.getRecords().stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), list);
    }

    @Override
    public QuestionDTO getById(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) throw new BizException(400, "题目不存在");
        return toDetail(q);
    }

    @Override
    @Transactional
    public Long create(QuestionDTO dto) {
        Question q = new Question();
        copyToEntity(dto, q);
        q.setStatus(0);  // 待审核
        q.setCreatedBy(UserContext.userId());
        questionMapper.insert(q);
        saveChildren(q.getId(), dto);
        return q.getId();
    }

    @Override
    @Transactional
    public void update(QuestionDTO dto) {
        Question q = questionMapper.selectById(dto.getId());
        if (q == null) throw new BizException(400, "题目不存在");
        copyToEntity(dto, q);
        q.setStatus(0);  // 修改后重新审核
        questionMapper.updateById(q);
        optionMapper.delete(new LambdaQueryWrapper<QuestionOption>().eq(QuestionOption::getQuestionId, dto.getId()));
        answerMapper.delete(new LambdaQueryWrapper<QuestionAnswer>().eq(QuestionAnswer::getQuestionId, dto.getId()));
        analysisMapper.delete(new LambdaQueryWrapper<QuestionAnalysis>().eq(QuestionAnalysis::getQuestionId, dto.getId()));
        saveChildren(dto.getId(), dto);
    }

    @Override
    public void delete(Long id) {
        questionMapper.deleteById(id);
    }

    @Override
    public void audit(Long id, Integer status, String remark) {
        Question q = new Question();
        q.setId(id);
        q.setStatus(status);
        q.setAuditRemark(remark);
        questionMapper.updateById(q);
    }

    @Override
    public List<QuestionDTO> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return questionMapper.selectBatchIds(ids).stream()
                .map(this::toDetail)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuestionDTO> countBySubject() {
        return List.of();
    }

    // ---------- helpers ----------

    private QuestionDTO toDetail(Question q) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(q.getId());
        dto.setSubjectId(q.getSubjectId());
        dto.setKnowledgePointId(q.getKnowledgePointId());
        dto.setQtype(q.getQtype());
        dto.setDifficulty(q.getDifficulty());
        dto.setCognitiveLevel(q.getCognitiveLevel());
        dto.setStem(q.getStem());
        dto.setScore(q.getScore());
        dto.setStatus(q.getStatus());

        Subject subj = subjectMapper.selectById(q.getSubjectId());
        if (subj != null) dto.setSubjectName(subj.getName());
        if (q.getKnowledgePointId() != null) {
            KnowledgePoint kp = kpMapper.selectById(q.getKnowledgePointId());
            if (kp != null) dto.setKnowledgePointName(kp.getName());
        }

        List<OptionDTO> opts = optionMapper.selectList(
                new LambdaQueryWrapper<QuestionOption>().eq(QuestionOption::getQuestionId, q.getId()))
                .stream().map(o -> {
                    OptionDTO od = new OptionDTO();
                    od.setKey(o.getOptionKey());
                    od.setText(o.getOptionText());
                    od.setCorrect(o.getIsCorrect() != null && o.getIsCorrect() == 1);
                    return od;
                }).collect(Collectors.toList());
        dto.setOptions(opts);

        List<AnswerDTO> answers = answerMapper.selectList(
                new LambdaQueryWrapper<QuestionAnswer>().eq(QuestionAnswer::getQuestionId, q.getId()))
                .stream().map(a -> {
                    AnswerDTO ad = new AnswerDTO();
                    ad.setType(a.getAnswerType());
                    ad.setText(a.getAnswerText());
                    return ad;
                }).collect(Collectors.toList());
        dto.setAnswers(answers);

        QuestionAnalysis an = analysisMapper.selectOne(
                new LambdaQueryWrapper<QuestionAnalysis>().eq(QuestionAnalysis::getQuestionId, q.getId()));
        if (an != null) dto.setAnalysis(an.getAnalysisText());
        return dto;
    }

    private void copyToEntity(QuestionDTO dto, Question q) {
        q.setSubjectId(dto.getSubjectId());
        q.setKnowledgePointId(dto.getKnowledgePointId());
        q.setQtype(dto.getQtype());
        q.setDifficulty(dto.getDifficulty() == null ? 2 : dto.getDifficulty());
        q.setCognitiveLevel(dto.getCognitiveLevel() == null ? "记忆" : dto.getCognitiveLevel());
        q.setStem(dto.getStem());
        q.setScore(dto.getScore() == null ? java.math.BigDecimal.ONE : dto.getScore());
    }

    private void saveChildren(Long qid, QuestionDTO dto) {
        if (dto.getOptions() != null) {
            for (OptionDTO o : dto.getOptions()) {
                QuestionOption qo = new QuestionOption();
                qo.setQuestionId(qid);
                qo.setOptionKey(o.getKey());
                qo.setOptionText(o.getText());
                qo.setIsCorrect(Boolean.TRUE.equals(o.getCorrect()) ? 1 : 0);
                optionMapper.insert(qo);
            }
        }
        if (dto.getAnswers() != null) {
            for (AnswerDTO a : dto.getAnswers()) {
                QuestionAnswer qa = new QuestionAnswer();
                qa.setQuestionId(qid);
                qa.setAnswerType(a.getType());
                qa.setAnswerText(a.getText());
                answerMapper.insert(qa);
            }
        }
        if (StringUtils.hasText(dto.getAnalysis())) {
            QuestionAnalysis qan = new QuestionAnalysis();
            qan.setQuestionId(qid);
            qan.setAnalysisText(dto.getAnalysis());
            analysisMapper.insert(qan);
        }
    }
}
