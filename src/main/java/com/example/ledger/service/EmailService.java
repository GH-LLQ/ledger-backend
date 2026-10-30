package com.example.ledger.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // 暂存验证码：key 是邮箱，value 是验证码
    private final Map<String, String> codeMap = new ConcurrentHashMap<>();

    // 1. 发送验证码
    public void sendCode(String email) {
        // 生成 6 位随机数
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 存入内存（真实生产环境建议存入 Redis 并设置过期时间）
        codeMap.put(email, code);

        // 构建邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("511823437@qq.com"); // 必须和 username 一致
        message.setTo(email);
        message.setSubject("【记账本】找回密码验证码");
        message.setText("您好，您正在进行密码重置操作。\n\n您的验证码是：【" + code + "】。\n\n验证码 5 分钟内有效。如非本人操作，请忽略此邮件。");

        // 发送
        mailSender.send(message);

        // 开发测试阶段可以打印在控制台看看
        System.out.println(">>> 发送验证码到 " + email + " : " + code);
    }

    // 2. 校验验证码
    public boolean verifyCode(String email, String code) {
        String savedCode = codeMap.get(email);
        if (savedCode != null && savedCode.equals(code)) {
            codeMap.remove(email); // 验证成功后销毁验证码，防止重复使用
            return true;
        }
        return false;
    }

}
