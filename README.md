# Ledger Backend - 个人财务管理系统 (后端)

基于 Spring Boot 3 + Spring Data JPA + MySQL 构建的个人财务管理系统后端服务。提供 JWT 鉴权、账单管理、账户余额联动、数据统计聚合、邮箱验证码找回密码等核心能力。

## 🛠 技术栈

- **核心框架**: Spring Boot 3.2.12
- **持久层**: Spring Data JPA (Hibernate), MySQL 8.0
- **安全认证**: Spring Security, JWT (jjwt 0.12.6), BCrypt
- **邮件服务**: Spring Boot Starter Mail (SMTP)
- **工具库**: Lombok, Bean Validation
- **部署**: Docker, Docker Compose

## ✨ 核心功能

1. **安全与认证**:
    - 无状态 JWT 认证，拦截恶意请求。
    - 注册时自动初始化用户的默认账户和分类。
    - 集成 JavaMail，实现真实的邮箱验证码发送与密码找回。
2. **账单管理 (Transactions)**:
    - 增删改查，支持跨端动态分页与多字段组合排序。
    - 强制 `userId` 数据隔离，杜绝越权访问。
    - 事务控制：记账/改账/删账时，实时同步对应账户余额，保证财务数据强一致性。
3. **数据统计 (Stats)**:
    - 月度收支汇总（收入/支出/结余）。
    - 分类占比聚合（饼图）。
    - 近 N 个月收支趋势聚合（折线图，原生 SQL `GROUP BY` 按月分组）。
4. **基础数据 (Meta)**:
    - 用户自定义分类管理（防重复删除校验）。
    - 账户管理（余额非手动修改，由业务流水驱动）。

## 🚀 本地运行指南

### 1. 环境要求
- JDK 17
- Maven 3.8+
- MySQL 8.0+

### 2. 数据库准备
执行根目录下的 `init.sql` 创建数据库和表结构：
```bash
mysql -u root -p < init.sql
```

### 3. 配置修改
修改 src/main/resources/application.yml 中的数据库连接信息：

```yaml
spring:
    datasource:
        url: jdbc:mysql://localhost:3306/finance_db?...
        username: root
        password: 你的密码
```

### 4.启动项目
```bash
./mvnw spring-boot:run
```
项目默认运行在 http://localhost:8080

