package com.example.ledger.repository;

import com.example.ledger.dto.TrendStatResponse;
import com.example.ledger.entity.Transaction;
import com.example.ledger.dto.CategoryStatResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction>{
    // 继承 JpaSpecificationExecutor 用于动态条件查询

    // 统计某个月的总收入
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId AND t.type = 'INCOME' AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumIncome(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // 统计某个月的总支出
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId AND t.type = 'EXPENSE' AND t.transactionDate BETWEEN :start AND :end")
    BigDecimal sumExpense(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    // 按分类聚合查询某个月的支出（JOIN 分类表获取名称）
    @Query("""
        SELECT new com.example.ledger.dto.CategoryStatResponse(
            c.id, c.name, SUM(t.amount)
        )
        FROM Transaction t
        JOIN Category c ON t.categoryId = c.id
        WHERE t.userId = :userId
          AND t.type = :type
          AND t.transactionDate BETWEEN :start AND :end
        GROUP BY c.id, c.name
        ORDER BY SUM(t.amount) DESC
    """)
    List<CategoryStatResponse> sumByCategory(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // 查询指定日期区间内的按月收支汇总（使用原生 SQL）
    @Query(value = """
        SELECT 
            DATE_FORMAT(t.transaction_date, '%Y-%m') AS month,
            COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) AS income,
            COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) AS expense
        FROM transactions t
        WHERE t.user_id = :userId AND t.transaction_date BETWEEN :startDate AND :endDate
        GROUP BY DATE_FORMAT(t.transaction_date, '%Y-%m')
        ORDER BY month ASC
    """, nativeQuery = true)
    List<Object[]> getMonthlyTrendRaw(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
