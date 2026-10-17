package com.tcm.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 30, message = "用户名长度3-30")
    private String username;
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度6-32")
    private String password;
    private String realName;
    private Long clazzId;
}
