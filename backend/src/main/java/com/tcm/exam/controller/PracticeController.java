package com.tcm.exam.controller;

import com.tcm.exam.common.Result;
import com.tcm.exam.dto.PracticeAnswerRequest;
import com.tcm.exam.dto.PracticeSessionRequest;
import com.tcm.exam.dto.SmartPaperRequest;
import com.tcm.exam.security.RequireRole;
import com.tcm.exam.service.PracticeService;
import com.tcm.exam.vo.PracticeExamVO;
import com.tcm.exam.vo.PracticeFeedbackVO;
import com.tcm.exam.vo.PracticeSessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practice")
@RequiredArgsConstructor
@RequireRole({"STUDENT"})
public class PracticeController {

    private final PracticeService practiceService;

    /**
     * 创建练习会话（章节/随机练习）
     */
    @PostMapping("/session")
    public Result<PracticeSessionVO> startSession(@RequestBody PracticeSessionRequest req) {
        return Result.ok(practiceService.startSession(req));
    }

    /**
     * 恢复练习会话（刷新页面）
     */
    @GetMapping("/session/{sessionId}")
    public Result<PracticeSessionVO> getSession(@PathVariable String sessionId) {
        return Result.ok(practiceService.getSession(sessionId));
    }

    /**
     * 单题作答：立即返回正确与否 + 标准答案 + 解析
     */
    @PostMapping("/session/{sessionId}/answer")
    public Result<PracticeFeedbackVO> gradeAnswer(@PathVariable String sessionId,
                                                  @RequestBody PracticeAnswerRequest req) {
        return Result.ok(practiceService.gradeAnswer(sessionId, req));
    }

    /**
     * 学生随机组卷考试：按规则抽题，返回可作答卷面
     */
    @PostMapping("/exam")
    public Result<PracticeExamVO> startExam(@RequestBody SmartPaperRequest req) {
        return Result.ok(practiceService.startExam(req));
    }
}
