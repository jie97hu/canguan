package cn.abcyun.canguan.expense.report.controller;

import javax.validation.Valid;

import cn.abcyun.canguan.expense.report.dto.CategoryBreakdownDto;
import cn.abcyun.canguan.expense.report.dto.CategoryBreakdownQueryRequest;
import cn.abcyun.canguan.expense.report.dto.ItemRankingDto;
import cn.abcyun.canguan.expense.report.dto.ItemRankingQueryRequest;
import cn.abcyun.canguan.expense.report.dto.OverviewDto;
import cn.abcyun.canguan.expense.report.dto.OverviewQueryRequest;
import cn.abcyun.canguan.expense.report.dto.StoreComparisonDto;
import cn.abcyun.canguan.expense.report.dto.StoreComparisonQueryRequest;
import cn.abcyun.canguan.expense.report.dto.TrendPointDto;
import cn.abcyun.canguan.expense.report.dto.TrendQueryRequest;
import cn.abcyun.canguan.expense.report.service.ReportService;
import cn.abcyun.canguan.expense.support.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/overview")
    public ApiResponse<OverviewDto> overview(@Valid OverviewQueryRequest request) {
        return ApiResponse.success(reportService.overview(request));
    }

    @GetMapping("/trend")
    public ApiResponse<java.util.List<TrendPointDto>> trend(@Valid TrendQueryRequest request) {
        return ApiResponse.success(reportService.trend(request));
    }

    @GetMapping("/category-breakdown")
    public ApiResponse<java.util.List<CategoryBreakdownDto>> categoryBreakdown(@Valid CategoryBreakdownQueryRequest request) {
        return ApiResponse.success(reportService.categoryBreakdown(request));
    }

    @GetMapping("/item-ranking")
    public ApiResponse<java.util.List<ItemRankingDto>> itemRanking(@Valid ItemRankingQueryRequest request) {
        return ApiResponse.success(reportService.itemRanking(request));
    }

    @GetMapping("/store-comparison")
    public ApiResponse<java.util.List<StoreComparisonDto>> storeComparison(@Valid StoreComparisonQueryRequest request) {
        return ApiResponse.success(reportService.storeComparison(request));
    }
}
