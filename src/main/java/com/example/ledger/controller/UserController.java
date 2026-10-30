package com.example.ledger.controller;

import com.example.ledger.dto.ApiResponse;
import com.example.ledger.dto.ResetPasswordRequest;
import com.example.ledger.dto.UserUpdateRequest;
import com.example.ledger.entity.User;
import com.example.ledger.repository.UserRepository;
import com.example.ledger.security.SecurityUtils;
import com.example.ledger.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    // 获取当前用户信息（包含邮箱，用于前端回显）
    @GetMapping("/me")
    public ApiResponse<User> getMe() {
        System.out.println("===== 进入了 getMe 接口 ====="); // 👈 加这行
        Long userId = SecurityUtils.getCurrentUserId();
        System.out.println("===== 解析到的 userId: " + userId + " ====="); // 👈 加这行

        User user = userRepository.findById(userId).orElseThrow();
        user.setPassword(null); // 不要把密码发给前端
        return ApiResponse.ok(user);
    }

    // 修改个人信息
    @PutMapping("/me")
    public ApiResponse<Void> updateProfile(@RequestBody UserUpdateRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updateProfile(userId, req);
        return ApiResponse.ok();
    }

}
