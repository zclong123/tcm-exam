package com.tcm.exam.controller;

import com.tcm.exam.common.Result;
import com.tcm.exam.security.RequireRole;
import com.tcm.exam.service.StatsService;
import com.tcm.exam.vo.ScoreVO;
import com.tcm.exam.vo.StatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/my-scores")
    public Result<List<ScoreVO>> myScores() {
        return Result.ok(statsService.myScores());
    }

    @GetMapping("/class")
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<StatsVO.ClassStatsVO> classStats(@RequestParam(required = false) Long examId,
                                                   @RequestParam(required = false) Long clazzId) {
        return Result.ok(statsService.classStats(examId, clazzId));
    }

    @GetMapping("/knowledge-mastery")
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<List<Map<String, Object>>> mastery(@RequestParam(required = false) Long examId,
                                                     @RequestParam(required = false) Long clazzId) {
        return Result.ok(statsService.knowledgeMastery(examId, clazzId));
    }

    @GetMapping("/question")
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<List<Map<String, Object>>> questionStats(@RequestParam(required = false) Long subjectId) {
        return Result.ok(statsService.questionStats(subjectId));
    }
}
