package com.tcm.exam.controller;

import com.tcm.exam.common.PageResult;
import com.tcm.exam.common.Result;
import com.tcm.exam.dto.SmartPaperRequest;
import com.tcm.exam.entity.Paper;
import com.tcm.exam.entity.PaperQuestion;
import com.tcm.exam.security.RequireRole;
import com.tcm.exam.service.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paper")
@RequiredArgsConstructor
public class PaperController {

    private final PaperService paperService;

    @PostMapping("/smart")
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<Paper> smart(@RequestBody SmartPaperRequest req) {
        return Result.ok(paperService.generateSmart(req));
    }

    @PostMapping("/manual")
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<Paper> manual(@RequestParam String name,
                                @RequestParam(required = false) Long subjectId,
                                @RequestBody List<Long> questionIds) {
        return Result.ok(paperService.createManual(name, subjectId, questionIds));
    }

    @GetMapping("/page")
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<PageResult<Paper>> page(@RequestParam(defaultValue = "1") int current,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) String name) {
        return Result.ok(paperService.page(current, size, name));
    }

    @GetMapping("/{id}")
    public Result<Paper> get(@PathVariable Long id) {
        return Result.ok(paperService.getById(id));
    }

    @GetMapping("/{id}/questions")
    public Result<List<PaperQuestion>> questions(@PathVariable Long id) {
        return Result.ok(paperService.listPaperQuestions(id));
    }

    @DeleteMapping("/{id}")
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<Void> delete(@PathVariable Long id) {
        paperService.delete(id);
        return Result.ok();
    }
}
