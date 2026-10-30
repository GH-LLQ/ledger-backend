package com.example.ledger.repository;

import com.example.ledger.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface CategoryRepository extends JpaRepository<Category, Long>{
    // 根据用户和类型（INCOME/EXPENSE）查询分类
    List<Category> findByUserIdAndType(Long userId, String type);
}
