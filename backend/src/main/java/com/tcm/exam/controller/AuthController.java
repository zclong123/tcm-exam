package com.tcm.exam.controller;

import com.tcm.exam.common.Result;
import com.tcm.exam.dto.LoginRequest;
import com.tcm.exam.dto.LoginResponse;
import com.tcm.exam.dto.RegisterRequest;
import com.tcm.exam.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.ok();
    }
}
