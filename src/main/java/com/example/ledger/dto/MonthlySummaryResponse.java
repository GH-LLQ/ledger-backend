package com.example.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

//接收统计结果
@Data
@AllArgsConstructor
public class MonthlySummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
}
