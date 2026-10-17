package com.tcm.exam.controller;

import com.tcm.exam.common.PageResult;
import com.tcm.exam.common.Result;
import com.tcm.exam.entity.SysUser;
import com.tcm.exam.security.RequireRole;
import com.tcm.exam.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<SysUser> me() {
        return Result.ok(userService.currentUser());
    }

    @GetMapping("/page")
    @RequireRole({"ADMIN"})
    public Result<PageResult<SysUser>> page(@RequestParam(defaultValue = "1") int current,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String roleCode,
                                            @RequestParam(required = false) Long clazzId) {
        return Result.ok(userService.page(current, size, keyword, roleCode, clazzId));
    }

    @PostMapping
    @RequireRole({"ADMIN"})
    public Result<Void> create(@RequestBody SysUser user) {
        userService.create(user);
        return Result.ok();
    }

    @PutMapping
    @RequireRole({"ADMIN"})
    public Result<Void> update(@RequestBody SysUser user) {
        userService.updateUser(user);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    @RequireRole({"ADMIN"})
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.ok();
    }
}
