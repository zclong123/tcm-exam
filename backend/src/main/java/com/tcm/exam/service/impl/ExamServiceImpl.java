package com.tcm.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcm.exam.common.BizException;
import com.tcm.exam.common.PageResult;
import com.tcm.exam.dto.QuestionDTO;
import com.tcm.exam.dto.SubmitAnswerRequest;
import com.tcm.exam.entity.*;
import com.tcm.exam.mapper.*;
import com.tcm.exam.security.UserContext;
import com.tcm.exam.service.ExamService;
import com.tcm.exam.service.QuestionService;
import com.tcm.exam.vo.ExamDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamMapper examMapper;
    private final ExamRecordMapper recordMapper;
    private final ExamAnswerMapper answerMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionService questionService;
    private final ObjectMapper objectMapper;

    @Override
    public Long create(Exam exam) {
        if (exam.getDurationMin() == null) exam.setDurationMin(90);
        if (exam.getPassScore() == null) exam.setPassScore(BigDecimal.valueOf(60));
        if (exam.getShuffle() == null) exam.setShuffle(true);
        if (exam.getAntiCheat() == null) exam.setAntiCheat(true);
        if (exam.getSwitchLimit() == null) exam.setSwitchLimit(3);
        if (exam.getStatus() == null) exam.setStatus(0);
        exam.setCreatedBy(UserContext.userId());
        examMapper.insert(exam);
        return exam.getId();
    }

    @Override
    public void update(Exam exam) {
        examMapper.updateById(exam);
    }

    @Override
    public void delete(Long id) {
        examMapper.deleteById(id);
    }

    @Override
    public PageResult<Exam> page(int current, int size, String name, Long clazzId) {
        LambdaQueryWrapper<Exam> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) qw.like(Exam::getName, name);
        if (clazzId != null) qw.eq(Exam::getClazzId, clazzId);
        qw.orderByDesc(Exam::getCreatedAt);
        return PageResult.of(examMapper.selectPage(new Page<>(current, size), qw));
    }

    @Override
    public List<Exam> studentExams() {
        Long userId = UserContext.userId();
        SysUser student = null;
        // 当前用户班级
        // 简化：通过 UserContext 无法直接拿班级，改为查询
        // 这里用 exam_record 反查较复杂，直接查全部进行中的考试
        LocalDateTime now = LocalDateTime.now();
        return examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .eq(Exam::getStatus, 1)
                .le(Exam::getStartTime, now)
                .ge(Exam::getEndTime, now)
                .orderByAsc(Exam::getStartTime));
    }

    @Override
    @Transactional
    public ExamDetailVO startExam(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) throw new BizException(400, "考试不存在");
        LocalDateTime now = LocalDateTime.now();
        if (exam.getStartTime() != null && now.isBefore(exam.getStartTime())) {
            throw new BizException(400, "考试尚未开始");
        }
        if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
            throw new BizException(400, "考试已结束");
        }

        Long studentId = UserContext.userId();
        // 幂等：若已有进行中记录则直接返回
        ExamRecord exist = recordMapper.selectOne(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getExamId, examId)
                .eq(ExamRecord::getStudentId, studentId)
                .in(ExamRecord::getStatus, "ING", "SUBMITTED", "AUTO")
                .last("LIMIT 1"));
        if (exist != null && !"ING".equals(exist.getStatus())) {
            throw new BizException(400, "你已完成本次考试");
        }

        ExamRecord record = exist != null ? exist : new ExamRecord();
        if (exist == null) {
            record.setExamId(examId);
            record.setStudentId(studentId);
            record.setStatus("ING");
            record.setStartTime(now);
            recordMapper.insert(record);
        }

        // 生成试卷快照（题目乱序）
        List<PaperQuestion> pqs = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, exam.getPaperId())
                        .orderByAsc(PaperQuestion::getSort));
        List<Long> qids = pqs.stream().map(PaperQuestion::getQuestionId).collect(Collectors.toList());
        List<QuestionDTO> questions = questionService.listByIds(qids);

        // 乱序题目
        if (Boolean.TRUE.equals(exam.getShuffle())) {
            Collections.shuffle(questions, new Random(System.currentTimeMillis()));
        }
        // 乱序选项
        if (Boolean.TRUE.equals(exam.getShuffle()) && questions != null) {
            for (QuestionDTO q : questions) {
                if (q.getOptions() != null && q.getOptions().size() > 1) {
                    Collections.shuffle(q.getOptions(), new Random(System.currentTimeMillis()));
                }
                q.setAnswers(null);  // 不暴露标准答案
            }
        }
        String snapshot = toJson(questions);

        // 保存快照（首次）
        if (exist == null) {
            record.setPaperSnapshot(snapshot);
            recordMapper.updateById(record);
        } else if (!StringUtils.hasText(record.getPaperSnapshot())) {
            record.setPaperSnapshot(snapshot);
            recordMapper.updateById(record);
        } else {
            // 断点续答：恢复快照
            questions = fromJson(record.getPaperSnapshot());
        }

        // 已作答
        Map<Long, ExamAnswer> answers = answerMapper.selectList(
                        new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getRecordId, record.getId()))
                .stream().collect(Collectors.toMap(ExamAnswer::getQuestionId, a -> a));

        ExamDetailVO vo = new ExamDetailVO();
        vo.setExam(exam);
        vo.setRecord(record);
        vo.setQuestions(questions);
        vo.setAnswers(answers);
        vo.setDurationMin(exam.getDurationMin());
        vo.setShuffle(exam.getShuffle());
        return vo;
    }

    @Override
    public void saveAnswer(Long recordId, SubmitAnswerRequest req) {
        ExamRecord record = getOwnRecord(recordId);
        if (!"ING".equals(record.getStatus())) {
            throw new BizException(400, "考试已结束，无法作答");
        }
        if (req.getAnswers() == null) return;
        for (SubmitAnswerRequest.AnswerItem item : req.getAnswers()) {
            ExamAnswer answer = answerMapper.selectOne(new LambdaQueryWrapper<ExamAnswer>()
                    .eq(ExamAnswer::getRecordId, recordId)
                    .eq(ExamAnswer::getQuestionId, item.getQuestionId()));
            if (answer == null) {
                answer = new ExamAnswer();
                answer.setRecordId(recordId);
                answer.setQuestionId(item.getQuestionId());
                answer.setStudentAnswer(item.getAnswer());
                answer.setIsMarked(item.getIsMarked());
                answerMapper.insert(answer);
            } else {
                answer.setStudentAnswer(item.getAnswer());
                answer.setIsMarked(item.getIsMarked());
                answerMapper.updateById(answer);
            }
        }
    }

    @Override
    @Transactional
    public void submit(Long recordId) {
        ExamRecord record = getOwnRecord(recordId);
        if (!"ING".equals(record.getStatus())) {
            throw new BizException(400, "考试已提交，请勿重复提交");
        }
        record.setStatus("SUBMITTED");
        record.setSubmitTime(LocalDateTime.now());
        recordMapper.updateById(record);
        // 判分在 GradingService 中异步完成（客观题立即，主观题待阅卷）
    }

    @Override
    public void reportSwitch(Long recordId) {
        ExamRecord record = getOwnRecord(recordId);
        record.setSwitchCount(record.getSwitchCount() == null ? 1 : record.getSwitchCount() + 1);
        recordMapper.updateById(record);
    }

    /**
     * 定时任务：每分钟检查超时未交卷的考试，自动交卷
     */
    @Override
    @Scheduled(cron = "${exam.auto-submit.cron:0 * * * * *}")
    public void autoSubmitOverdue() {
        List<Exam> exams = examMapper.selectList(new LambdaQueryWrapper<Exam>().eq(Exam::getStatus, 1));
        LocalDateTime now = LocalDateTime.now();
        for (Exam exam : exams) {
            List<ExamRecord> ing = recordMapper.selectList(new LambdaQueryWrapper<ExamRecord>()
                    .eq(ExamRecord::getExamId, exam.getId())
                    .eq(ExamRecord::getStatus, "ING"));
            for (ExamRecord record : ing) {
                boolean overdue = false;
                if (record.getStartTime() != null && exam.getDurationMin() != null) {
                    LocalDateTime deadline = record.getStartTime().plusMinutes(exam.getDurationMin());
                    if (now.isAfter(deadline)) overdue = true;
                }
                if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
                    overdue = true;
                }
                if (overdue) {
                    record.setStatus("AUTO");
                    record.setSubmitTime(now);
                    recordMapper.updateById(record);
                    log.info("自动交卷: record={}", record.getId());
                }
            }
        }
    }

    @Override
    public ExamRecord getRecord(Long recordId) {
        return getOwnRecord(recordId);
    }

    private ExamRecord getOwnRecord(Long recordId) {
        ExamRecord record = recordMapper.selectById(recordId);
        if (record == null) throw new BizException(400, "考试记录不存在");
        if (!record.getStudentId().equals(UserContext.userId())) {
            throw new BizException(403, "无权访问他人的考试记录");
        }
        return record;
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new BizException(500, "数据序列化失败");
        }
    }

    private List<QuestionDTO> fromJson(String s) {
        try {
            return objectMapper.readValue(s, new com.fasterxml.jackson.core.type.TypeReference<List<QuestionDTO>>() {});
        } catch (Exception e) {
            throw new BizException(500, "试卷快照解析失败");
        }
    }
}
