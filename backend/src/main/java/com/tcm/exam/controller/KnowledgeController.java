package com.tcm.exam.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tcm.exam.common.Result;
import com.tcm.exam.entity.KnowledgePoint;
import com.tcm.exam.entity.Subject;
import com.tcm.exam.mapper.KnowledgePointMapper;
import com.tcm.exam.mapper.SubjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final SubjectMapper subjectMapper;
    private final KnowledgePointMapper kpMapper;

    @GetMapping("/subjects")
    public Result<List<Subject>> subjects() {
        return Result.ok(subjectMapper.selectList(new LambdaQueryWrapper<Subject>()
                .orderByAsc(Subject::getSort)));
    }

    @GetMapping("/points")
    public Result<List<KnowledgePoint>> points(@RequestParam(required = false) Long subjectId) {
        LambdaQueryWrapper<KnowledgePoint> qw = new LambdaQueryWrapper<>();
        if (subjectId != null) qw.eq(KnowledgePoint::getSubjectId, subjectId);
        return Result.ok(kpMapper.selectList(qw.orderByAsc(KnowledgePoint::getId)));
    }
}
