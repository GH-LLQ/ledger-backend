package com.example.ledger.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {
    @NotNull(message = "账户不能为空")
    private Long accountId;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "类型不能为空")
    private String type; // INCOME / EXPENSE

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    @NotNull(message = "日期不能为空")
    private LocalDate transactionDate;

    private String note;
}
