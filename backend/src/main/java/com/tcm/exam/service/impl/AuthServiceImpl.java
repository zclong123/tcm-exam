package com.tcm.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tcm.exam.common.BizException;
import com.tcm.exam.dto.LoginRequest;
import com.tcm.exam.dto.LoginResponse;
import com.tcm.exam.dto.RegisterRequest;
import com.tcm.exam.entity.SysUser;
import com.tcm.exam.mapper.SysUserMapper;
import com.tcm.exam.security.JwtUtil;
import com.tcm.exam.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(400, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(400, "账号已被禁用");
        }
        String token = jwtUtil.createToken(user.getId(), user.getUsername(), user.getRoleCode());

        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setRealName(user.getRealName());
        resp.setRoleCode(user.getRoleCode());
        resp.setClazzId(user.getClazzId());
        return resp;
    }

    @Override
    public void register(RegisterRequest request) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername()));
        if (exists != null && exists > 0) {
            throw new BizException(400, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(StringUtils.hasText(request.getRealName()) ? request.getRealName() : request.getUsername());
        user.setClazzId(request.getClazzId());
        user.setRoleCode("STUDENT");
        user.setStatus(1);
        userMapper.insert(user);
    }
}
