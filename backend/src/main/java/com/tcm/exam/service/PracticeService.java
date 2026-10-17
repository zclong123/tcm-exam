package com.tcm.exam.service;

import com.tcm.exam.dto.PracticeAnswerRequest;
import com.tcm.exam.dto.PracticeSessionRequest;
import com.tcm.exam.dto.SmartPaperRequest;
import com.tcm.exam.vo.PracticeExamVO;
import com.tcm.exam.vo.PracticeFeedbackVO;
import com.tcm.exam.vo.PracticeSessionVO;

public interface PracticeService {

    /**
     * 创建练习会话（章节/随机），返回净化后的题目
     */
    PracticeSessionVO startSession(PracticeSessionRequest req);

    /**
     * 恢复练习会话（刷新页面后）
     */
    PracticeSessionVO getSession(String sessionId);

    /**
     * 单题判分：客观题立即判对错 + 返回标准答案与解析，主观题返回参考答案（correct=null）
     */
    PracticeFeedbackVO gradeAnswer(String sessionId, PracticeAnswerRequest req);

    /**
     * 学生随机组卷考试：按规则抽题、生成 exam_record（examId 为空），返回可作答的卷面
     */
    PracticeExamVO startExam(SmartPaperRequest req);
}
