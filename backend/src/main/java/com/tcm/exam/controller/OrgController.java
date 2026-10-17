package com.tcm.exam.controller;

import com.tcm.exam.common.Result;
import com.tcm.exam.entity.Clazz;
import com.tcm.exam.entity.Department;
import com.tcm.exam.service.OrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/org")
@RequiredArgsConstructor
public class OrgController {

    private final OrgService orgService;

    @GetMapping("/departments")
    public Result<List<Department>> departments() {
        return Result.ok(orgService.listDepartments());
    }

    @GetMapping("/clazzes")
    public Result<List<Clazz>> clazzes(@RequestParam(required = false) Long departmentId) {
        return Result.ok(orgService.listClazzes(departmentId));
    }
}
