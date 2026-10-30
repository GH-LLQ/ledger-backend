package com.example.ledger.controller;

import com.example.ledger.dto.ApiResponse;
import com.example.ledger.dto.MonthlySummaryResponse;
import com.example.ledger.dto.CategoryStatResponse;
import com.example.ledger.dto.TrendStatResponse;
import com.example.ledger.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // 获取月度概览
    @GetMapping("/summary")
    public ApiResponse<MonthlySummaryResponse> getSummary(@RequestParam String month) {
        return ApiResponse.ok(statsService.getMonthlySummary(month));
    }

    // 分类占比
    @GetMapping("/category")
    public ApiResponse<List<CategoryStatResponse>> getCategoryStats(
            @RequestParam String month,
            @RequestParam(defaultValue = "EXPENSE") String type) {
        return ApiResponse.ok(statsService.getCategoryStats(month, type));
    }

    // 趋势折线图
    @GetMapping("/trend")
    public ApiResponse<List<TrendStatResponse>> getTrend(@RequestParam(defaultValue = "6") int months) {
        return ApiResponse.ok(statsService.getTrend(months));
    }
}
