package com.tcm.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcm.exam.common.BizException;
import com.tcm.exam.common.PageResult;
import com.tcm.exam.dto.AnswerDTO;
import com.tcm.exam.dto.GradeResult;
import com.tcm.exam.dto.OptionDTO;
import com.tcm.exam.dto.QuestionDTO;
import com.tcm.exam.entity.ExamAnswer;
import com.tcm.exam.entity.ExamRecord;
import com.tcm.exam.entity.Score;
import com.tcm.exam.entity.Question;
import com.tcm.exam.mapper.*;
import com.tcm.exam.service.GradingService;
import com.tcm.exam.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradingServiceImpl implements GradingService {

    private static final Set<String> OBJECTIVE_TYPES = Set.of("A", "X", "判断", "填空", "B");

    private final ExamAnswerMapper answerMapper;
    private final ExamRecordMapper recordMapper;
    private final ScoreMapper scoreMapper;
    private final QuestionMapper questionMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionService questionService;

    @Override
    @Transactional
    public void autoGrade(Long recordId) {
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) return;

        List<ExamAnswer> answers = answerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getRecordId, recordId));
        if (answers.isEmpty()) return;

        // 题目信息批量加载
        List<Long> qids = answers.stream().map(ExamAnswer::getQuestionId).distinct().collect(Collectors.toList());
        Map<Long, QuestionDTO> qmap = questionService.listByIds(qids).stream()
                .collect(Collectors.toMap(QuestionDTO::getId, q -> q));

        BigDecimal objectiveScore = BigDecimal.ZERO;
        for (ExamAnswer answer : answers) {
            QuestionDTO q = qmap.get(answer.getQuestionId());
            if (q == null) continue;
            if (OBJECTIVE_TYPES.contains(q.getQtype())) {
                boolean ok = checkObjective(q.getQtype(), q, answer.getStudentAnswer());
                answer.setIsCorrect(ok);
                answer.setScore(ok ? q.getScore() : BigDecimal.ZERO);
                answer.setReviewStatus("AUTO");
                answerMapper.updateById(answer);
                if (ok) {
                    objectiveScore = objectiveScore.add(q.getScore());
                } else {
                    addWrong(record, q);
                }
            } else {
                // 主观题：待批阅
                answer.setReviewStatus("NONE");
                answerMapper.updateById(answer);
            }
        }

        // 成绩汇总
        Score score = scoreMapper.selectOne(new LambdaQueryWrapper<Score>()
                .eq(Score::getRecordId, recordId));
        if (score == null) {
            score = new Score();
            score.setExamId(record.getExamId());
            score.setStudentId(record.getStudentId());
            score.setRecordId(recordId);
        }
        score.setTotalScore(objectiveScore);
        score.setCreatedAt(LocalDateTime.now());
        scoreMapper.insertOrUpdate(score);

        record.setTotalScore(objectiveScore);
        record.setStatus("SUBMITTED");
        recordMapper.updateById(record);
        log.info("自动判分完成: record={}, 客观题得分={}", recordId, objectiveScore);
    }

    private void addWrong(ExamRecord record, QuestionDTO q) {
        var wq = wrongQuestionMapper.selectOne(new LambdaQueryWrapper<com.tcm.exam.entity.WrongQuestion>()
                .eq(com.tcm.exam.entity.WrongQuestion::getStudentId, record.getStudentId())
                .eq(com.tcm.exam.entity.WrongQuestion::getQuestionId, q.getId()));
        if (wq == null) {
            com.tcm.exam.entity.WrongQuestion n = new com.tcm.exam.entity.WrongQuestion();
            n.setStudentId(record.getStudentId());
            n.setQuestionId(q.getId());
            n.setSourceType("EXAM");
            n.setSourceExamId(record.getExamId());
            n.setWrongTimes(1);
            n.setReviewInterval(1);
            wrongQuestionMapper.insert(n);
        } else {
            wq.setWrongTimes(wq.getWrongTimes() + 1);
            wrongQuestionMapper.updateById(wq);
        }
    }

    @Override
    public void review(Long answerId, Double score, String comment) {
        ExamAnswer answer = answerMapper.selectById(answerId);
        if (answer == null) throw new BizException(400, "作答记录不存在");
        answer.setScore(BigDecimal.valueOf(score));
        answer.setReviewComment(comment);
        answer.setReviewStatus("REVIEWED");
        answer.setReviewedAt(LocalDateTime.now());
        answerMapper.updateById(answer);

        // 重算总分
        recomputeRecordTotal(answer.getRecordId());
    }

    private void recomputeRecordTotal(Long recordId) {
        List<ExamAnswer> answers = answerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getRecordId, recordId));
        BigDecimal total = BigDecimal.ZERO;
        for (ExamAnswer a : answers) {
            if (a.getScore() != null) total = total.add(a.getScore());
        }
        ExamRecord record = recordMapper.selectById(recordId);
        record.setTotalScore(total);
        record.setStatus("GRADED");
        recordMapper.updateById(record);

        Score score = scoreMapper.selectOne(new LambdaQueryWrapper<Score>()
                .eq(Score::getRecordId, recordId));
        if (score != null) {
            score.setTotalScore(total);
            score.setIsPass(record.getExamId() != null);
            scoreMapper.updateById(score);
        }
    }

    @Override
    public PageResult<ExamAnswer> reviewList(int current, int size, Long examId, Long recordId) {
        LambdaQueryWrapper<ExamAnswer> qw = new LambdaQueryWrapper<>();
        if (recordId != null) {
            qw.eq(ExamAnswer::getRecordId, recordId);
        }
        if (examId != null) {
            // 通过 record 关联 exam
            List<Long> recordIds = recordMapper.selectList(new LambdaQueryWrapper<ExamRecord>()
                            .eq(ExamRecord::getExamId, examId))
                    .stream().map(ExamRecord::getId).collect(Collectors.toList());
            if (recordIds.isEmpty()) return PageResult.of(0, List.of());
            qw.in(ExamAnswer::getRecordId, recordIds);
        }
        qw.in(ExamAnswer::getReviewStatus, "NONE").orderByAsc(ExamAnswer::getId);
        Page<ExamAnswer> page = answerMapper.selectPage(new Page<>(current, size), qw);
        return PageResult.of(page);
    }

    @Override
    public double questionFullScore(Long questionId) {
        Question q = questionMapper.selectById(questionId);
        return q != null && q.getScore() != null ? q.getScore().doubleValue() : 1.0;
    }

    @Override
    public boolean checkObjective(String qtype, QuestionDTO question, String studentAnswer) {
        if (studentAnswer == null || studentAnswer.isBlank()) return false;
        String std = studentAnswer.trim();
        String correct = correctAnswerText(question);
        if (correct == null) return false;

        return switch (qtype) {
            case "A", "B", "判断" -> std.equalsIgnoreCase(correct.trim());
            case "X" -> {
                // 多选：全对得分
                yield normalize(std).equals(normalize(correct));
            }
            case "填空" -> std.contains(correct.trim()) || correct.trim().contains(std);
            default -> false;
        };
    }

    @Override
    public GradeResult gradeOne(QuestionDTO question, String studentAnswer) {
        String correct = correctAnswerText(question);
        if (correct == null) {
            return new GradeResult(false, null);
        }
        return new GradeResult(checkObjective(question.getQtype(), question, studentAnswer), correct);
    }

    @Override
    public String correctAnswerText(QuestionDTO q) {
        // 客观题优先从 answers 取标准答案文本；若无则从选项 correct=true 推导
        if (q.getAnswers() != null && !q.getAnswers().isEmpty()) {
            return q.getAnswers().stream().map(AnswerDTO::getText)
                    .collect(Collectors.joining());
        }
        if (q.getOptions() != null) {
            return q.getOptions().stream().filter(OptionDTO::getCorrect)
                    .map(OptionDTO::getKey).sorted().collect(Collectors.joining());
        }
        return null;
    }

    private String normalize(String s) {
        return s.toUpperCase().replaceAll("[^A-Z]", "");
    }
}
