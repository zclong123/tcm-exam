package com.tcm.exam.controller;

import com.tcm.exam.common.PageResult;
import com.tcm.exam.common.Result;
import com.tcm.exam.entity.ExamAnswer;
import com.tcm.exam.security.RequireRole;
import com.tcm.exam.service.GradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grading")
@RequiredArgsConstructor
public class GradingController {

    private final GradingService gradingService;

    @PostMapping("/auto/{recordId}")
    public Result<Void> auto(@PathVariable Long recordId) {
        gradingService.autoGrade(recordId);
        return Result.ok();
    }

    @GetMapping("/review-list")
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<PageResult<ExamAnswer>> reviewList(@RequestParam(defaultValue = "1") int current,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) Long examId,
                                                     @RequestParam(required = false) Long recordId) {
        return Result.ok(gradingService.reviewList(current, size, examId, recordId));
    }

    @PostMapping("/review")
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<Void> review(@RequestParam Long answerId,
                               @RequestParam Double score,
                               @RequestParam(required = false) String comment) {
        gradingService.review(answerId, score, comment);
        return Result.ok();
    }
}
