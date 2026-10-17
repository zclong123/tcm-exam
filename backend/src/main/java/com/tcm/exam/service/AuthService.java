package com.tcm.exam.service;

import com.tcm.exam.dto.LoginRequest;
import com.tcm.exam.dto.LoginResponse;
import com.tcm.exam.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);
}
