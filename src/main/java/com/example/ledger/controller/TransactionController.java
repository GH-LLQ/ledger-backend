package com.example.ledger.controller;

import com.example.ledger.dto.ApiResponse;
import com.example.ledger.dto.TransactionRequest;
import com.example.ledger.entity.Transaction;
import com.example.ledger.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // 新增记录
    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody TransactionRequest req) {
        transactionService.create(req);
        return ApiResponse.ok();
    }

    // 列表查询
    @GetMapping
    public ApiResponse<Page<Transaction>> query(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String keyword,
            // 👇 新增：接收排序字段和方向
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Transaction> result = transactionService.query(type, categoryId, startDate, endDate, keyword, sortField, sortOrder, page, size);
        return ApiResponse.ok(result);
    }

    // 删除记录
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ApiResponse.ok();
    }

    // 更新记录
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody TransactionRequest req) {
        transactionService.update(id, req);
        return ApiResponse.ok();
    }
}
