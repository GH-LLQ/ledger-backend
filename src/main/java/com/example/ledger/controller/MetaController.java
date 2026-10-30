package com.example.ledger.controller;

import com.example.ledger.dto.ApiResponse;
import com.example.ledger.entity.Account;
import com.example.ledger.entity.Category;
import com.example.ledger.repository.AccountRepository;
import com.example.ledger.repository.CategoryRepository;
import com.example.ledger.repository.TransactionRepository;
import com.example.ledger.security.SecurityUtils;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/meta")
public class MetaController {

    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public MetaController(CategoryRepository categoryRepository,
                          AccountRepository accountRepository,
                          TransactionRepository transactionRepository) {
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // 获取分类下拉列表
    @GetMapping("/categories")
    public ApiResponse<List<Category>> getCategories(@RequestParam String type) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.ok(categoryRepository.findByUserIdAndType(userId, type));
    }

    // 获取账户下拉列表
    @GetMapping("/accounts")
    public ApiResponse<List<Account>> getAccounts() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.ok(accountRepository.findByUserId(userId));
    }

    // ====== 新增：分类管理 CRUD ======

    // 1. 新增分类
    @PostMapping("/categories")
    public ApiResponse<Void> createCategory(@RequestBody Category category) {
        Long userId = SecurityUtils.getCurrentUserId();
        category.setId(null); // 防止前端传 ID 覆盖
        category.setUserId(userId);
        categoryRepository.save(category);
        return ApiResponse.ok();
    }

    // 2. 修改分类
    @PutMapping("/categories/{id}")
    public ApiResponse<Void> updateCategory(@PathVariable Long id, @RequestBody Category categoryReq) {
        Long userId = SecurityUtils.getCurrentUserId();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        if (!category.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改该分类");
        }

        category.setName(categoryReq.getName());
        category.setIcon(categoryReq.getIcon());
        category.setType(categoryReq.getType());
        categoryRepository.save(category);
        return ApiResponse.ok();
    }

    // 3. 删除分类
    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        if (!category.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该分类");
        }

        // 业务校验：如果有交易记录正在使用这个分类，不允许删除
        // 这里为了简单，我们先写死一个查询（真实环境应写一个 count 查询）
        long count = transactionRepository.count((root, query, cb) ->
                cb.and(cb.equal(root.get("categoryId"), id))
        );
        if (count > 0) {
            throw new RuntimeException("该分类下已有记账记录，无法删除");
        }

        categoryRepository.delete(category);
        return ApiResponse.ok();
    }

    // ====== 账户管理 CRUD ======

    // 1. 新增账户
    @PostMapping("/accounts")
    public ApiResponse<Void> createAccount(@RequestBody Account accountReq) {
        Long userId = SecurityUtils.getCurrentUserId();
        Account account = new Account();
        account.setUserId(userId);
        account.setName(accountReq.getName());
        account.setType(accountReq.getType());
        account.setBalance(accountReq.getBalance() != null ? accountReq.getBalance() : BigDecimal.ZERO);
        accountRepository.save(account);
        return ApiResponse.ok();
    }

    // 2. 修改账户（只允许改名字和类型，余额不让随便改）
    @PutMapping("/accounts/{id}")
    public ApiResponse<Void> updateAccount(@PathVariable Long id, @RequestBody Account accountReq) {
        Long userId = SecurityUtils.getCurrentUserId();
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("账户不存在"));

        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改该账户");
        }

        account.setName(accountReq.getName());
        account.setType(accountReq.getType());
        // 注意：这里故意不设置 balance，防止用户乱改余额
        accountRepository.save(account);
        return ApiResponse.ok();
    }

    // 3. 删除账户
    @DeleteMapping("/accounts/{id}")
    public ApiResponse<Void> deleteAccount(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("账户不存在"));

        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除该账户");
        }

        // 业务校验：如果账户下有记账记录，不允许删除
        long count = transactionRepository.count((root, query, cb) ->
                cb.equal(root.get("accountId"), id)
        );
        if (count > 0) {
            throw new RuntimeException("该账户下已有记账记录，无法删除");
        }

        accountRepository.delete(account);
        return ApiResponse.ok();
    }
}
