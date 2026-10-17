package com.tcm.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tcm.exam.common.BizException;
import com.tcm.exam.common.PageResult;
import com.tcm.exam.dto.SmartPaperRequest;
import com.tcm.exam.entity.Paper;
import com.tcm.exam.entity.PaperQuestion;
import com.tcm.exam.entity.Question;
import com.tcm.exam.mapper.PaperMapper;
import com.tcm.exam.mapper.PaperQuestionMapper;
import com.tcm.exam.mapper.QuestionMapper;
import com.tcm.exam.security.UserContext;
import com.tcm.exam.service.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 组卷服务
 * 智能组卷：遍历题型规则，按难度/知识点约束从已审核题目中随机抽题，避免重复。
 */
@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;

    @Override
    public List<Question> draftQuestions(SmartPaperRequest req) {
        if (req.getTypeRules() == null || req.getTypeRules().isEmpty()) {
            throw new BizException(400, "请配置至少一种题型");
        }
        Set<Long> used = new HashSet<>();
        List<Question> result = new ArrayList<>();

        for (SmartPaperRequest.TypeRule rule : req.getTypeRules()) {
            int need = rule.getCount() == null ? 0 : rule.getCount();
            int got = 0;
            List<Long> kps = req.getKnowledgePoints();
            Integer difficulty = rule.getDifficulty() != null ? rule.getDifficulty() : req.getDifficulty();
            // 若指定知识点则逐点抽取，否则全范围抽取
            if (kps != null && !kps.isEmpty()) {
                for (Long kpId : kps) {
                    int each = (int) Math.ceil((need - got) / (double) Math.max(1, kps.size()));
                    List<Question> picked = pick(rule.getQtype(), req.getSubjectId(), difficulty, kpId, each, used);
                    result.addAll(picked);
                    got += picked.size();
                    if (got >= need) break;
                }
            } else {
                int before = result.size();
                result.addAll(pick(rule.getQtype(), req.getSubjectId(), difficulty, null, need, used));
                got = result.size() - before;
            }
            // 若某题型题量不足，放宽难度再抽一次
            if (got < need) {
                List<Question> extra = pick(rule.getQtype(), req.getSubjectId(), null, null, need - got, used);
                result.addAll(extra);
                got += extra.size();
            }
        }

        if (result.isEmpty()) {
            throw new BizException(400, "未抽到符合条件的题目，请检查题库与组卷规则");
        }
        return result;
    }

    @Override
    @Transactional
    public Paper generateSmart(SmartPaperRequest req) {
        List<Question> questions = draftQuestions(req);

        int sort = 1;
        BigDecimal total = BigDecimal.ZERO;
        List<PaperQuestion> pqList = new ArrayList<>();
        for (Question q : questions) {
            PaperQuestion pq = toPq(req, ruleFor(q, req), q, sort++);
            pqList.add(pq);
            total = total.add(pq.getScore());
        }

        Paper paper = new Paper();
        paper.setName(StringUtils.hasText(req.getName()) ? req.getName() : "智能试卷-" + System.currentTimeMillis());
        paper.setPaperType("SMART");
        paper.setTotalScore(total);
        paper.setQuestionCount(pqList.size());
        paper.setSubjectId(req.getSubjectId());
        paper.setDifficulty(req.getDifficulty());
        paper.setRuleJson(toJson(req));
        paper.setCreatedBy(UserContext.userId());
        paper.setStatus(0);
        paperMapper.insert(paper);

        for (PaperQuestion pq : pqList) {
            pq.setPaperId(paper.getId());
            paperQuestionMapper.insert(pq);
        }
        return paper;
    }

    private SmartPaperRequest.TypeRule ruleFor(Question q, SmartPaperRequest req) {
        return req.getTypeRules().stream()
                .filter(r -> r.getQtype().equals(q.getQtype()))
                .findFirst()
                .orElse(req.getTypeRules().get(0));
    }

    private PaperQuestion toPq(SmartPaperRequest req, SmartPaperRequest.TypeRule rule, Question q, int sort) {
        PaperQuestion pq = new PaperQuestion();
        pq.setQuestionId(q.getId());
        pq.setSort(sort);
        Double score = rule.getScore();
        if (score == null) score = 1.0;
        pq.setScore(BigDecimal.valueOf(score));
        return pq;
    }

    /**
     * 从已审核题目中随机抽 need 道，排除 used 中的重复
     */
    private List<Question> pick(String qtype, Long subjectId, Integer difficulty, Long kpId, int need, Set<Long> used) {
        if (need <= 0) return List.of();
        int fetch = Math.max(need * 3, need + 20);  // 多取一些再过滤，保证随机性
        List<Question> candidates = questionMapper.randomPick(qtype, subjectId, difficulty, kpId, fetch);
        List<Question> result = new ArrayList<>();
        for (Question q : candidates) {
            if (used.contains(q.getId())) continue;
            used.add(q.getId());
            result.add(q);
            if (result.size() >= need) break;
        }
        return result;
    }

    private String toJson(SmartPaperRequest req) {
        // 简易 JSON 序列化，避免引入额外依赖
        StringBuilder sb = new StringBuilder();
        sb.append("{\"subjectId\":").append(req.getSubjectId())
          .append(",\"difficulty\":").append(req.getDifficulty())
          .append(",\"rules\":[");
        if (req.getTypeRules() != null) {
            for (int i = 0; i < req.getTypeRules().size(); i++) {
                SmartPaperRequest.TypeRule r = req.getTypeRules().get(i);
                if (i > 0) sb.append(",");
                sb.append("{\"qtype\":\"").append(r.getQtype())
                  .append("\",\"count\":").append(r.getCount())
                  .append(",\"score\":").append(r.getScore() == null ? 1.0 : r.getScore())
                  .append(",\"difficulty\":").append(r.getDifficulty()).append("}");
            }
        }
        sb.append("]}");
        return sb.toString();
    }

    @Override
    @Transactional
    public Paper createManual(String name, Long subjectId, List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            throw new BizException(400, "请选择题目");
        }
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        Paper paper = new Paper();
        paper.setName(name);
        paper.setPaperType("MANUAL");
        paper.setSubjectId(subjectId);
        paper.setCreatedBy(UserContext.userId());
        paper.setStatus(0);
        paper.setQuestionCount(questions.size());
        BigDecimal total = BigDecimal.ZERO;
        for (Question q : questions) {
            total = total.add(q.getScore() == null ? BigDecimal.ONE : q.getScore());
        }
        paper.setTotalScore(total);
        paperMapper.insert(paper);
        int sort = 1;
        for (Question q : questions) {
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(paper.getId());
            pq.setQuestionId(q.getId());
            pq.setSort(sort++);
            pq.setScore(q.getScore() == null ? BigDecimal.ONE : q.getScore());
            paperQuestionMapper.insert(pq);
        }
        return paper;
    }

    @Override
    public PageResult<Paper> page(int current, int size, String name) {
        LambdaQueryWrapper<Paper> qw = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) qw.like(Paper::getName, name);
        qw.orderByDesc(Paper::getId);
        return PageResult.of(paperMapper.selectPage(new Page<>(current, size), qw));
    }

    @Override
    public Paper getById(Long id) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null) throw new BizException(400, "试卷不存在");
        return paper;
    }

    @Override
    public List<PaperQuestion> listPaperQuestions(Long paperId) {
        return paperQuestionMapper.selectList(new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getPaperId, paperId).orderByAsc(PaperQuestion::getSort));
    }

    @Override
    public void delete(Long id) {
        paperQuestionMapper.delete(new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, id));
        paperMapper.deleteById(id);
    }
}
