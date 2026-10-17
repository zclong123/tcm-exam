package com.tcm.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcm.exam.common.BizException;
import com.tcm.exam.dto.OptionDTO;
import com.tcm.exam.dto.PracticeAnswerRequest;
import com.tcm.exam.dto.PracticeSessionRequest;
import com.tcm.exam.dto.QuestionDTO;
import com.tcm.exam.dto.SmartPaperRequest;
import com.tcm.exam.entity.ExamRecord;
import com.tcm.exam.entity.Question;
import com.tcm.exam.entity.QuestionStat;
import com.tcm.exam.entity.WrongQuestion;
import com.tcm.exam.mapper.ExamRecordMapper;
import com.tcm.exam.mapper.QuestionMapper;
import com.tcm.exam.mapper.QuestionStatMapper;
import com.tcm.exam.mapper.WrongQuestionMapper;
import com.tcm.exam.security.PracticeSessionStore;
import com.tcm.exam.security.UserContext;
import com.tcm.exam.service.GradingService;
import com.tcm.exam.service.PaperService;
import com.tcm.exam.service.PracticeService;
import com.tcm.exam.service.QuestionService;
import com.tcm.exam.vo.PracticeExamVO;
import com.tcm.exam.vo.PracticeFeedbackVO;
import com.tcm.exam.vo.PracticeSessionVO;
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
public class PracticeServiceImpl implements PracticeService {

    private static final Set<String> OBJECTIVE_TYPES = Set.of("A", "X", "判断", "填空", "B");
    private static final List<String> DEFAULT_MIX = List.of("A", "X", "判断", "填空", "B", "名解", "简答", "论述", "病案");
    private static final int DEFAULT_COUNT = 10;
    private static final int MAX_COUNT = 100;

    private final QuestionMapper questionMapper;
    private final QuestionService questionService;
    private final GradingService gradingService;
    private final PaperService paperService;
    private final PracticeSessionStore sessionStore;
    private final ExamRecordMapper recordMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionStatMapper statMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PracticeSessionVO startSession(PracticeSessionRequest req) {
        int count = req.getCount() == null ? DEFAULT_COUNT : req.getCount();
        if (count <= 0 || count > MAX_COUNT) {
            throw new BizException(400, "题量需在 1~" + MAX_COUNT + " 之间");
        }
        String mode = req.getMode() == null ? "RANDOM" : req.getMode();
        List<Question> questions = switch (mode) {
            case "CHAPTER" -> drawChapter(req, count);
            case "RANDOM" -> drawRandom(req, count);
            default -> throw new BizException(400, "未知练习模式");
        };

        // 净化 + 乱序
        List<QuestionDTO> sanitized = questions.stream().map(this::toDTO).map(this::sanitize).collect(Collectors.toList());

        PracticeSessionVO vo = new PracticeSessionVO();
        vo.setMode(mode);
        vo.setCount(sanitized.size());
        vo.setQuestions(sanitized);
        vo.setSessionId(sessionStore.create(UserContext.userId(),
                sanitized.stream().map(QuestionDTO::getId).collect(Collectors.toList())));
        return vo;
    }

    @Override
    public PracticeSessionVO getSession(String sessionId) {
        PracticeSessionStore.Session session = requireSession(sessionId);
        List<Long> qids = session.questionIds();
        List<QuestionDTO> sanitized = questionService.listByIds(qids).stream()
                .map(this::sanitize)
                .collect(Collectors.toList());
        PracticeSessionVO vo = new PracticeSessionVO();
        vo.setSessionId(sessionId);
        vo.setMode("CHAPTER");
        vo.setCount(sanitized.size());
        vo.setQuestions(sanitized);
        return vo;
    }

    @Override
    @Transactional
    public PracticeFeedbackVO gradeAnswer(String sessionId, PracticeAnswerRequest req) {
        PracticeSessionStore.Session session = requireSession(sessionId);
        if (req.getQuestionId() == null) throw new BizException(400, "题目编号不能为空");
        if (!session.questionIds().contains(req.getQuestionId())) {
            throw new BizException(403, "该题目不属于当前练习会话");
        }

        QuestionDTO q = questionService.getById(req.getQuestionId());
        if (q == null) throw new BizException(400, "题目不存在");

        PracticeFeedbackVO vo = new PracticeFeedbackVO();
        vo.setQuestionId(q.getId());
        vo.setAnalysis(q.getAnalysis());
        vo.setScore(BigDecimal.ZERO);

        if (OBJECTIVE_TYPES.contains(q.getQtype())) {
            var grade = gradingService.gradeOne(q, req.getAnswer());
            vo.setCorrect(grade.correct());
            vo.setCorrectAnswer(grade.correctAnswer());
            vo.setScore(grade.correct() ? q.getScore() : BigDecimal.ZERO);
            if (!grade.correct()) {
                addWrong(q.getId());
            }
            upsertStat(q.getId(), grade.correct());
        } else {
            // 主观题：不自动判分，仅返回参考答案与解析
            vo.setCorrect(null);
            vo.setCorrectAnswer(gradingService.correctAnswerText(q));
        }
        return vo;
    }

    @Override
    @Transactional
    public PracticeExamVO startExam(SmartPaperRequest req) {
        List<Question> questions = paperService.draftQuestions(req);
        Long studentId = UserContext.userId();

        // 净化 + 乱序
        List<QuestionDTO> sanitized = questions.stream().map(this::toDTO).map(this::sanitize).collect(Collectors.toList());

        ExamRecord record = new ExamRecord();
        record.setExamId(null);            // 随机组卷练习，不关联正式考试
        record.setStudentId(studentId);
        record.setStatus("ING");
        record.setStartTime(LocalDateTime.now());
        record.setPaperSnapshot(toJson(sanitized));
        recordMapper.insert(record);

        PracticeExamVO vo = new PracticeExamVO();
        vo.setRecord(record);
        vo.setQuestions(sanitized);
        vo.setDurationMin(req.getTypeRules().stream()
                .mapToInt(r -> r.getCount() == null ? 0 : r.getCount()).sum() / 3 + 30);
        return vo;
    }

    // ---------- 抽题 ----------

    private List<Question> drawChapter(PracticeSessionRequest req, int count) {
        Long kpId = req.getKnowledgePointId();
        if (kpId == null && req.getSubjectId() == null) {
            throw new BizException(400, "章节练习请选择学科或知识点");
        }
        Set<Long> used = new HashSet<>();
        List<Question> result = pick(req.getQtype(), req.getSubjectId(), kpId, count, used);
        if (result.isEmpty()) throw new BizException(400, "未抽到符合条件的题目，请调整筛选条件");
        return result;
    }

    private List<Question> drawRandom(PracticeSessionRequest req, int count) {
        Set<Long> used = new HashSet<>();
        if (req.getQtype() != null && !req.getQtype().isBlank()) {
            List<Question> result = pick(req.getQtype(), req.getSubjectId(), null, count, used);
            if (result.isEmpty()) throw new BizException(400, "未抽到符合条件的题目，请调整筛选条件");
            return result;
        }
        // 混合题型均分
        List<Question> result = new ArrayList<>();
        int perType = (int) Math.ceil(count / (double) DEFAULT_MIX.size());
        for (String qtype : DEFAULT_MIX) {
            if (result.size() >= count) break;
            int need = Math.min(perType, count - result.size());
            result.addAll(pick(qtype, req.getSubjectId(), null, need, used));
        }
        if (result.isEmpty()) throw new BizException(400, "未抽到符合条件的题目，请调整筛选条件");
        return result;
    }

    /**
     * 随机抽 need 道，排除 used 中的重复；多取一些保证随机性。
     * qtype 可空（练习场景不限题型）
     */
    private List<Question> pick(String qtype, Long subjectId, Long kpId, int need, Set<Long> used) {
        if (need <= 0) return List.of();
        int fetch = Math.max(need * 3, need + 20);
        List<Question> candidates = questionMapper.randomPickAny(qtype, subjectId, null, kpId, fetch);
        List<Question> result = new ArrayList<>();
        for (Question q : candidates) {
            if (used.contains(q.getId())) continue;
            used.add(q.getId());
            result.add(q);
            if (result.size() >= need) break;
        }
        return result;
    }

    // ---------- 净化 / 转换 ----------

    private QuestionDTO toDTO(Question q) {
        return questionService.getById(q.getId());
    }

    private QuestionDTO sanitize(QuestionDTO q) {
        q.setAnswers(null);
        q.setAnalysis(null);
        if (q.getOptions() != null && !q.getOptions().isEmpty()) {
            for (OptionDTO o : q.getOptions()) o.setCorrect(null);
            Collections.shuffle(q.getOptions(), new Random(System.currentTimeMillis()));
        }
        return q;
    }

    // ---------- 错题 / 统计 ----------

    private void addWrong(Long questionId) {
        Long studentId = UserContext.userId();
        WrongQuestion wq = wrongQuestionMapper.selectOne(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getStudentId, studentId)
                .eq(WrongQuestion::getQuestionId, questionId));
        if (wq == null) {
            WrongQuestion n = new WrongQuestion();
            n.setStudentId(studentId);
            n.setQuestionId(questionId);
            n.setSourceType("PRACTICE");
            n.setWrongTimes(1);
            n.setReviewInterval(1);
            n.setMastered(false);
            wrongQuestionMapper.insert(n);
        } else {
            wq.setWrongTimes((wq.getWrongTimes() == null ? 0 : wq.getWrongTimes()) + 1);
            wrongQuestionMapper.updateById(wq);
        }
    }

    private void upsertStat(Long questionId, boolean correct) {
        QuestionStat st = statMapper.selectOne(new LambdaQueryWrapper<QuestionStat>()
                .eq(QuestionStat::getQuestionId, questionId));
        if (st == null) {
            st = new QuestionStat();
            st.setQuestionId(questionId);
            st.setAnswerCount(1);
            st.setCorrectCount(correct ? 1 : 0);
        } else {
            st.setAnswerCount((st.getAnswerCount() == null ? 0 : st.getAnswerCount()) + 1);
            st.setCorrectCount((st.getCorrectCount() == null ? 0 : st.getCorrectCount()) + (correct ? 1 : 0));
        }
        int answered = st.getAnswerCount();
        st.setCorrectRate(answered == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(st.getCorrectCount()).divide(BigDecimal.valueOf(answered), 4, RoundingMode.HALF_UP));
        st.setUpdatedAt(LocalDateTime.now());
        statMapper.insertOrUpdate(st);
    }

    // ---------- 会话校验 / JSON ----------

    private PracticeSessionStore.Session requireSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) throw new BizException(400, "练习会话不存在");
        PracticeSessionStore.Session s = sessionStore.getOwn(sessionId, UserContext.userId());
        if (s == null) throw new BizException(400, "练习会话已失效，请重新开始");
        return s;
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new BizException(500, "数据序列化失败");
        }
    }
}
