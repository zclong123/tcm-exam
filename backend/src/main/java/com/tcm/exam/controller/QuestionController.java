package com.tcm.exam.controller;

import com.tcm.exam.common.PageResult;
import com.tcm.exam.common.Result;
import com.tcm.exam.dto.QuestionDTO;
import com.tcm.exam.dto.QuestionQueryRequest;
import com.tcm.exam.security.RequireRole;
import com.tcm.exam.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping("/page")
    public Result<PageResult<QuestionDTO>> page(QuestionQueryRequest req) {
        return Result.ok(questionService.page(req));
    }

    @GetMapping("/batch")
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<List<QuestionDTO>> batch(@RequestParam List<Long> ids) {
        return Result.ok(questionService.listByIds(ids));
    }

    @GetMapping("/{id}")
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<QuestionDTO> get(@PathVariable Long id) {
        return Result.ok(questionService.getById(id));
    }

    @PostMapping
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<Long> create(@RequestBody QuestionDTO dto) {
        return Result.ok(questionService.create(dto));
    }

    @PutMapping
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<Void> update(@RequestBody QuestionDTO dto) {
        questionService.update(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/audit")
    @RequireRole({"TEACHER", "ADMIN"})
    public Result<Void> audit(@PathVariable Long id, @RequestParam Integer status,
                              @RequestParam(required = false) String remark) {
        questionService.audit(id, status, remark);
        return Result.ok();
    }
}
