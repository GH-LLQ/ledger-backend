package com.example.ledger.service;

import com.example.ledger.dto.CategoryStatResponse;
import com.example.ledger.dto.MonthlySummaryResponse;
import com.example.ledger.dto.TrendStatResponse;
import com.example.ledger.repository.CategoryRepository;
import com.example.ledger.repository.TransactionRepository;
import com.example.ledger.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;


@Service // 👈 必须有这行！
public class StatsService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public StatsService(TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    // 1. 月度概览（收入/支出/结余）
    public MonthlySummaryResponse getMonthlySummary(String monthStr) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 解析月份，比如 "2026-10"
        YearMonth month = YearMonth.parse(monthStr);
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        BigDecimal income = transactionRepository.sumIncome(userId, start, end);
        BigDecimal expense = transactionRepository.sumExpense(userId, start, end);
        BigDecimal balance = income.subtract(expense);

        return new MonthlySummaryResponse(income, expense, balance);
    }

    // 2. 分类占比统计
    public List<CategoryStatResponse> getCategoryStats(String monthStr, String type) {
        Long userId = SecurityUtils.getCurrentUserId();
        YearMonth month = YearMonth.parse(monthStr);
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        // type 默认传 EXPENSE（支出饼图），也可以传 INCOME
        return transactionRepository.sumByCategory(userId, type, start, end);
    }

    // 3. 近 N 个月的收支趋势
    public List<TrendStatResponse> getTrend(int months) {
        Long userId = SecurityUtils.getCurrentUserId();

        // 计算起始日期：往前推 N 个月的第一天
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        LocalDate startDate = LocalDate.now().minusMonths(months - 1).withDayOfMonth(1);

        List<Object[]> results = transactionRepository.getMonthlyTrendRaw(userId, startDate, endDate);

        // 手动转换 Object[] 到 DTO
        List<TrendStatResponse> list = new ArrayList<>();
        for (Object[] row : results) {
            String month = (String) row[0];
            BigDecimal income = (BigDecimal) row[1];
            BigDecimal expense = (BigDecimal) row[2];
            list.add(new TrendStatResponse(month, income, expense));
        }
        return list;
    }
}
