package com.example.ledger.service;

import com.example.ledger.dto.LoginRequest;
import com.example.ledger.dto.LoginResponse;
import com.example.ledger.dto.RegisterRequest;
import com.example.ledger.entity.Account;
import com.example.ledger.entity.Category;
import com.example.ledger.entity.User;
import com.example.ledger.repository.AccountRepository;
import com.example.ledger.repository.CategoryRepository;
import com.example.ledger.repository.UserRepository;
import com.example.ledger.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AccountRepository accountRepository,
                       CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    public void register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        userRepository.save(user);

        // ====== 新增：注册成功后，自动初始化账户和分类 ======
        Long userId = user.getId();
        // 初始化账户
        accountRepository.saveAll(List.of(
                createAccount(userId, "现金", "CASH"),
                createAccount(userId, "支付宝", "ALIPAY")
        ));

        // 初始化分类
        categoryRepository.saveAll(List.of(
                createCategory(userId, "餐饮", "EXPENSE"),
                createCategory(userId, "交通", "EXPENSE"),
                createCategory(userId, "购物", "EXPENSE"),
                createCategory(userId, "工资", "INCOME"),
                createCategory(userId, "奖金", "INCOME")
        ));
    }

    // 辅助方法：快速建账户
    private Account createAccount(Long userId, String name, String type) {
        Account acc = new Account();
        acc.setUserId(userId);
        acc.setName(name);
        acc.setType(type);
        acc.setBalance(BigDecimal.ZERO);
        return acc;
    }

    // 辅助方法：快速建分类
    private Category createCategory(Long userId, String name, String type) {
        Category cat = new Category();
        cat.setUserId(userId);
        cat.setName(name);
        cat.setType(type);
        return cat;
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername());
    }
}
