package com.example.ledger.controller;

import com.example.ledger.dto.*;
import com.example.ledger.security.SecurityUtils;
import com.example.ledger.service.AuthService;
import com.example.ledger.service.UserService;
import com.example.ledger.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService; // 👈 新增
    private final EmailService emailService;

    public AuthController(AuthService authService, UserService userService, EmailService emailService) {

        this.authService = authService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ApiResponse<Long> me() {
        return ApiResponse.ok(SecurityUtils.getCurrentUserId());
    }

    // 暴露在 AuthController 下，因为不需要登录
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest req) {
        userService.resetPassword(req);
        return ApiResponse.ok();
    }

    // 发送验证码
    @PostMapping("/send-code")
    public ApiResponse<Void> sendCode(@RequestParam String email) {
        emailService.sendCode(email);
        return ApiResponse.ok();
    }
}
