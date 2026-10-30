package com.example.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank
    private String email;

    @NotBlank
    private String code; // 👈 新增验证码

    @NotBlank
    private String newPassword;
}