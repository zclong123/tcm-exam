package com.tcm.exam.controller;

import com.tcm.exam.common.PageResult;
import com.tcm.exam.common.Result;
import com.tcm.exam.dto.SubmitAnswerRequest;
import com.tcm.exam.entity.Exam;
import com.tcm.exam.entity.ExamRecord;
import com.tcm.exam.security.RequireRole;
import com.tcm.exam.service.ExamService;
import com.tcm.exam.vo.ExamDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    // ---- 教师/管理员 ----
    @PostMapping
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<Long> create(@RequestBody Exam exam) {
        return Result.ok(examService.create(exam));
    }

    @PutMapping
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<Void> update(@RequestBody Exam exam) {
        examService.update(exam);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<Void> delete(@PathVariable Long id) {
        examService.delete(id);
        return Result.ok();
    }

    @GetMapping("/page")
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<PageResult<Exam>> page(@RequestParam(defaultValue = "1") int current,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String name,
                                         @RequestParam(required = false) Long clazzId) {
        return Result.ok(examService.page(current, size, name, clazzId));
    }

    // ---- 学生 ----
    @GetMapping("/mine")
    public Result<List<Exam>> mine() {
        return Result.ok(examService.studentExams());
    }

    @PostMapping("/start/{examId}")
    public Result<ExamDetailVO> start(@PathVariable Long examId) {
        return Result.ok(examService.startExam(examId));
    }

    @PostMapping("/record/{recordId}/save")
    public Result<Void> save(@PathVariable Long recordId, @RequestBody SubmitAnswerRequest req) {
        examService.saveAnswer(recordId, req);
        return Result.ok();
    }

    @PostMapping("/record/{recordId}/submit")
    public Result<Void> submit(@PathVariable Long recordId) {
        examService.submit(recordId);
        return Result.ok();
    }

    @PostMapping("/record/{recordId}/switch")
    public Result<Void> reportSwitch(@PathVariable Long recordId) {
        examService.reportSwitch(recordId);
        return Result.ok();
    }

    @GetMapping("/record/{recordId}")
    public Result<ExamRecord> record(@PathVariable Long recordId) {
        return Result.ok(examService.getRecord(recordId));
    }
}
