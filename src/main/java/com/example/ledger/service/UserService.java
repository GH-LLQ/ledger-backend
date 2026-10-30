package com.example.ledger.service;

import com.example.ledger.dto.UserUpdateRequest;
import com.example.ledger.dto.ResetPasswordRequest;
import com.example.ledger.entity.User;
import com.example.ledger.service.EmailService;
import com.example.ledger.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // 1. 修改个人信息
    public void updateProfile(Long userId, UserUpdateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }
        userRepository.save(user);
    }

    // 2. 忘记密码（通过用户名和邮箱验证）
    public void resetPassword(ResetPasswordRequest req) {
        // 1. 校验验证码
        if (!emailService.verifyCode(req.getEmail(), req.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 2. 根据用户名查询用户
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或邮箱不匹配"));

        // 3. 校验该用户绑定的邮箱是否匹配
        if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(req.getEmail())) {
            throw new RuntimeException("用户名或邮箱不匹配");
        }

        // 4. 修改密码
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }
}
