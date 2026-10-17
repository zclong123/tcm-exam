package com.tcm.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tcm.exam.entity.*;
import com.tcm.exam.mapper.*;
import com.tcm.exam.security.UserContext;
import com.tcm.exam.service.StatsService;
import com.tcm.exam.vo.ScoreVO;
import com.tcm.exam.vo.StatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final ScoreMapper scoreMapper;
    private final ExamMapper examMapper;
    private final ExamRecordMapper recordMapper;
    private final ExamAnswerMapper answerMapper;
    private final SysUserMapper userMapper;
    private final ClazzMapper clazzMapper;
    private final QuestionMapper questionMapper;
    private final QuestionStatMapper statMapper;
    private final KnowledgePointMapper kpMapper;

    @Override
    public List<ScoreVO> myScores() {
        Long studentId = UserContext.userId();
        List<Score> scores = scoreMapper.selectList(new LambdaQueryWrapper<Score>()
                .eq(Score::getStudentId, studentId).orderByDesc(Score::getCreatedAt));
        return scores.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public StatsVO.ClassStatsVO classStats(Long examId, Long clazzId) {
        List<Long> studentIds = null;
        if (clazzId != null) {
            studentIds = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getClazzId, clazzId))
                    .stream().map(SysUser::getId).collect(Collectors.toList());
        }
        List<Score> scores = scoreMapper.selectList(new LambdaQueryWrapper<Score>()
                .eq(examId != null, Score::getExamId, examId)
                .in(studentIds != null && !studentIds.isEmpty(), Score::getStudentId, studentIds)
                .orderByDesc(Score::getTotalScore));

        StatsVO.ClassStatsVO vo = new StatsVO.ClassStatsVO();
        vo.setStudentCount(scores.size());
        if (!scores.isEmpty()) {
            BigDecimal sum = scores.stream().map(Score::getTotalScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setAvgScore(sum.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP));
            vo.setMaxScore(scores.get(0).getTotalScore());
            vo.setMinScore(scores.get(scores.size() - 1).getTotalScore());
            long pass = scores.stream().filter(s -> Boolean.TRUE.equals(s.getIsPass())).count();
            vo.setPassRate(BigDecimal.valueOf(pass).divide(BigDecimal.valueOf(scores.size()), 4, RoundingMode.HALF_UP));
        }
        List<ScoreVO> all = scores.stream().map(this::toVO).collect(Collectors.toList());
        vo.setAllScores(all);
        vo.setTopScores(all.size() > 10 ? all.subList(0, 10) : all);
        return vo;
    }

    @Override
    public List<Map<String, Object>> knowledgeMastery(Long examId, Long clazzId) {
        LambdaQueryWrapper<ExamRecord> qw = new LambdaQueryWrapper<>();
        if (examId != null) qw.eq(ExamRecord::getExamId, examId);
        if (clazzId != null) {
            List<Long> studentIds = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getClazzId, clazzId))
                    .stream().map(SysUser::getId).collect(Collectors.toList());
            if (!studentIds.isEmpty()) qw.in(ExamRecord::getStudentId, studentIds);
        }
        List<ExamRecord> records = recordMapper.selectList(qw);
        if (records.isEmpty()) return List.of();

        List<Long> recordIds = records.stream().map(ExamRecord::getId).collect(Collectors.toList());
        List<ExamAnswer> answers = answerMapper.selectList(new LambdaQueryWrapper<ExamAnswer>()
                .in(ExamAnswer::getRecordId, recordIds));

        Map<Long, Question> qmap = new HashMap<>();
        for (ExamAnswer a : answers) {
            if (qmap.containsKey(a.getQuestionId())) continue;
            Question q = questionMapper.selectById(a.getQuestionId());
            if (q != null) qmap.put(a.getQuestionId(), q);
        }

        Map<Long, long[]> kpStat = new HashMap<>();
        for (ExamAnswer a : answers) {
            Question q = qmap.get(a.getQuestionId());
            if (q == null || q.getKnowledgePointId() == null) continue;
            long[] arr = kpStat.computeIfAbsent(q.getKnowledgePointId(), k -> new long[2]);
            arr[0]++;
            if (Boolean.TRUE.equals(a.getIsCorrect())) arr[1]++;
        }
        return kpStat.entrySet().stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("knowledgePointId", e.getKey());
            KnowledgePoint kp = kpMapper.selectById(e.getKey());
            m.put("knowledgePointName", kp == null ? "未知知识点" : kp.getName());
            long[] v = e.getValue();
            m.put("correctRate", v[0] == 0 ? 0 : BigDecimal.valueOf(v[1])
                    .divide(BigDecimal.valueOf(v[0]), 4, RoundingMode.HALF_UP));
            m.put("answerCount", v[0]);
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> questionStats(Long subjectId) {
        List<Question> questions = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(subjectId != null, Question::getSubjectId, subjectId)
                .orderByDesc(Question::getId)
                .last("LIMIT 500"));
        return questions.stream().map(q -> {
            Map<String, Object> m = new HashMap<>();
            QuestionStat st = statMapper.selectOne(new LambdaQueryWrapper<QuestionStat>()
                    .eq(QuestionStat::getQuestionId, q.getId()));
            m.put("questionId", q.getId());
            m.put("qtype", q.getQtype());
            m.put("difficulty", q.getDifficulty());
            m.put("answerCount", st == null ? 0 : st.getAnswerCount());
            m.put("correctRate", st == null ? null : st.getCorrectRate());
            m.put("difficultyReal", st == null ? null : st.getDifficultyReal());
            m.put("discrimination", st == null ? null : st.getDiscrimination());
            return m;
        }).collect(Collectors.toList());
    }

    private ScoreVO toVO(Score s) {
        ScoreVO vo = new ScoreVO();
        vo.setScoreId(s.getId());
        vo.setExamId(s.getExamId());
        vo.setStudentId(s.getStudentId());
        vo.setTotalScore(s.getTotalScore());
        vo.setIsPass(s.getIsPass());
        vo.setRankNo(s.getRankNo());
        vo.setCreatedAt(s.getCreatedAt());
        Exam exam = examMapper.selectById(s.getExamId());
        if (exam != null) {
            vo.setExamName(exam.getName());
        } else {
            vo.setExamName("随机组卷练习");
        }
        SysUser u = userMapper.selectById(s.getStudentId());
        if (u != null) {
            vo.setStudentName(u.getRealName());
            Clazz c = u.getClazzId() != null ? clazzMapper.selectById(u.getClazzId()) : null;
            if (c != null) vo.setClazzName(c.getName());
        }
        return vo;
    }
}
