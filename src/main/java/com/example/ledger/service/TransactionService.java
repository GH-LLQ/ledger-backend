package com.example.ledger.service;

import com.example.ledger.dto.TransactionRequest;
import com.example.ledger.entity.Transaction;
import com.example.ledger.entity.Account;
import com.example.ledger.repository.TransactionRepository;
import com.example.ledger.repository.AccountRepository;
import com.example.ledger.security.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository; // 👈 新增

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    // 新增记录
    @Transactional // 👈 建议加上事务注解，保证账单入库和余额修改要么全成功，要么全失败
    public void create(TransactionRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setAccountId(req.getAccountId());
        tx.setCategoryId(req.getCategoryId());
        tx.setType(req.getType());
        tx.setAmount(req.getAmount());
        tx.setTransactionDate(req.getTransactionDate());
        tx.setNote(req.getNote());
        transactionRepository.save(tx);
        // 👇 新增：更新账户余额
        updateAccountBalance(req.getAccountId(), req.getType(), req.getAmount(), true);
    }


    // 分页 + 动态筛选 + 动态排序
    public Page<Transaction> query(String type, Long categoryId, LocalDate startDate, LocalDate endDate, String keyword,
                                   String sortField, String sortOrder,
                                   int page, int size) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 1. 安全处理排序字段（防止前端传空或非法字段）
        String field = "transactionDate"; // 默认字段
        if (sortField != null && !sortField.isBlank()) {
            if (List.of("transactionDate", "amount", "id").contains(sortField)) {
                field = sortField;
            }
        }

        // 2. 安全处理排序方向
        Sort.Direction direction = Sort.Direction.DESC; // 默认降序
        if ("asc".equalsIgnoreCase(sortOrder) || "ascend".equalsIgnoreCase(sortOrder)) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, field));

        // 3. 动态筛选
        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), userId));

            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoryId"), categoryId));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), endDate));
            }
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(root.get("note"), "%" + keyword + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return transactionRepository.findAll(spec, pageable);
    }

    // 删除记录
    public void delete(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 先查出这条记录（如果不存在，抛异常）
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

        // 核心防越权：检查这条记录是不是属于当前登录用户
        if (!tx.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该记录");
        }

        // 👇 删除了账单，要把钱退回到账户
        updateAccountBalance(tx.getAccountId(), tx.getType(), tx.getAmount(), false);
        transactionRepository.delete(tx);
    }

    // 更新记录
    public void update(Long id, TransactionRequest req) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 查出现有记录
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

        // 防越权校验
        if (!tx.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改该记录");
        }

        // 👇 1. 先把旧账单的影响撤销（反向操作）
        updateAccountBalance(tx.getAccountId(), tx.getType(), tx.getAmount(), false);

        // 更新字段
        tx.setAccountId(req.getAccountId());
        tx.setCategoryId(req.getCategoryId());
        tx.setType(req.getType());
        tx.setAmount(req.getAmount());
        tx.setTransactionDate(req.getTransactionDate());
        tx.setNote(req.getNote());

        transactionRepository.save(tx);
        // 👇 2. 再把新账单的影响加上
        updateAccountBalance(req.getAccountId(), req.getType(), req.getAmount(), true);
    }

    // 更新账户余额的辅助方法
    private void updateAccountBalance(Long accountId, String type, BigDecimal amount, boolean isAdd) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new RuntimeException("账户不存在"));

        BigDecimal changeAmount = "INCOME".equals(type) ? amount : amount.negate();

        // 如果是撤销（删除/修改前），金额取反
        if (!isAdd) {
            changeAmount = changeAmount.negate();
        }

        account.setBalance(account.getBalance().add(changeAmount));
        accountRepository.save(account);
    }
}
