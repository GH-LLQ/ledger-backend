package com.example.ledger.dto;
//分类统计结果的
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CategoryStatResponse {
    private Long categoryId;
    private String categoryName;
    private BigDecimal totalAmount;
}
