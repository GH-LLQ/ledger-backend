package com.example.ledger.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TrendStatResponse {
    private String month;       // 例如 "2026-10"
    private BigDecimal income;  // 该月总收入
    private BigDecimal expense; // 该月总支出
}