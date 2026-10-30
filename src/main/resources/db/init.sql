-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS finance_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE finance_db;

-- 2. 用户表
CREATE TABLE IF NOT EXISTS users (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  username    VARCHAR(50)  NOT NULL                COMMENT '用户名',
  password    VARCHAR(100) NOT NULL                COMMENT 'BCrypt加密密码',
  email       VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 3. 账户表
CREATE TABLE IF NOT EXISTS accounts (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '账户ID',
  user_id     BIGINT        NOT NULL                COMMENT '所属用户',
  name        VARCHAR(50)   NOT NULL                COMMENT '账户名称',
  type        VARCHAR(20)   NOT NULL                COMMENT '账户类型 CASH/BANK/ALIPAY/WECHAT',
  balance     DECIMAL(12,2) NOT NULL DEFAULT 0.00   COMMENT '当前余额',
  created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_user (user_id),
  CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账户表';

-- 4. 分类表
CREATE TABLE IF NOT EXISTS categories (
  id       BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  user_id  BIGINT      NOT NULL                COMMENT '所属用户',
  name     VARCHAR(50) NOT NULL                COMMENT '分类名称',
  type     VARCHAR(10) NOT NULL                COMMENT '类型 INCOME/EXPENSE',
  icon     VARCHAR(50) DEFAULT NULL            COMMENT '图标标识',
  PRIMARY KEY (id),
  KEY idx_user_type (user_id, type),
  CONSTRAINT fk_categories_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- 5. 交易记录表
CREATE TABLE IF NOT EXISTS transactions (
  id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  user_id           BIGINT        NOT NULL                COMMENT '所属用户',
  account_id        BIGINT        NOT NULL                COMMENT '账户',
  category_id       BIGINT        NOT NULL                COMMENT '分类',
  type              VARCHAR(10)   NOT NULL                COMMENT '类型 INCOME/EXPENSE',
  amount            DECIMAL(12,2) NOT NULL                COMMENT '金额',
  transaction_date  DATE          NOT NULL                COMMENT '交易日期',
  note              VARCHAR(255)  DEFAULT NULL            COMMENT '备注',
  created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_user_date (user_id, transaction_date),
  KEY idx_user_category (user_id, category_id),
  CONSTRAINT fk_tx_user     FOREIGN KEY (user_id)     REFERENCES users(id)      ON DELETE CASCADE,
  CONSTRAINT fk_tx_account  FOREIGN KEY (account_id)  REFERENCES accounts(id),
  CONSTRAINT fk_tx_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易记录表';

-- 6. 预算表 (用于后续扩展)
CREATE TABLE IF NOT EXISTS budgets (
  id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '预算ID',
  user_id      BIGINT        NOT NULL                COMMENT '所属用户',
  category_id  BIGINT        DEFAULT NULL            COMMENT '分类,NULL表示总预算',
  month        VARCHAR(7)    NOT NULL                COMMENT '月份 2026-10',
  amount       DECIMAL(12,2) NOT NULL                COMMENT '预算金额',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_category_month (user_id, category_id, month),
  CONSTRAINT fk_budgets_user     FOREIGN KEY (user_id)     REFERENCES users(id)     ON DELETE CASCADE,
  CONSTRAINT fk_budgets_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预算表';
