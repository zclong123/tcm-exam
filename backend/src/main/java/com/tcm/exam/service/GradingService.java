package com.tcm.exam.service;

import com.tcm.exam.common.PageResult;
import com.tcm.exam.dto.GradeResult;
import com.tcm.exam.dto.QuestionDTO;
import com.tcm.exam.entity.ExamAnswer;

import java.util.List;

public interface GradingService {
    /**
     * 对一次考试记录进行自动判分（客观题）
     * 返回已判分的客观题得分
     */
    void autoGrade(Long recordId);

    /**
     * 教师人工批阅主观题
     */
    void review(Long answerId, Double score, String comment);

    /**
     * 教师待批阅列表
     */
    PageResult<ExamAnswer> reviewList(int current, int size, Long examId, Long recordId);

    /**
     * 计算主观题单题满分
     */
    double questionFullScore(Long questionId);

    /**
     * 对比答案是否匹配（客观题判分核心）
     */
    boolean checkObjective(String qtype, QuestionDTO question, String studentAnswer);

    /**
     * 单题判分（客观题）：返回是否正确 + 标准答案文本
     * 主观题不应调用（无自动判分标准）
     */
    GradeResult gradeOne(QuestionDTO question, String studentAnswer);

    /**
     * 推导标准答案文本（供练习立即反馈展示）
     * 客观题返回正确选项字母/答案文本；无则返回 null
     */
    String correctAnswerText(QuestionDTO question);
}
