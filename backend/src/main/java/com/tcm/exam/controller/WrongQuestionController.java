package com.tcm.exam.controller;

import com.tcm.exam.common.PageResult;
import com.tcm.exam.common.Result;
import com.tcm.exam.dto.QuestionDTO;
import com.tcm.exam.service.WrongQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wrong")
@RequiredArgsConstructor
public class WrongQuestionController {

    private final WrongQuestionService wrongQuestionService;

    @GetMapping("/page")
    public Result<PageResult<QuestionDTO>> page(@RequestParam(defaultValue = "1") int current,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) Long knowledgePointId,
                                                @RequestParam(required = false) Boolean mastered) {
        return Result.ok(wrongQuestionService.page(current, size, knowledgePointId, mastered));
    }

    @PostMapping("/{wrongId}/mastered")
    public Result<Void> markMastered(@PathVariable Long wrongId) {
        wrongQuestionService.markMastered(wrongId);
        return Result.ok();
    }

    @PostMapping("/{wrongId}/redo")
    public Result<Void> redo(@PathVariable Long wrongId) {
        wrongQuestionService.redo(wrongId);
        return Result.ok();
    }
}
