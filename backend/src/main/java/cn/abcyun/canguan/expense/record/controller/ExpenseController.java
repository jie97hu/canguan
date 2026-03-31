package cn.abcyun.canguan.expense.record.controller;

import javax.validation.Valid;

import cn.abcyun.canguan.expense.record.dto.ExpenseHistoryDto;
import cn.abcyun.canguan.expense.record.dto.ExpenseItemOptionQueryRequest;
import cn.abcyun.canguan.expense.record.dto.ExpenseQueryRequest;
import cn.abcyun.canguan.expense.record.dto.ExpenseRecordDto;
import cn.abcyun.canguan.expense.record.dto.ExpenseUpsertRequest;
import cn.abcyun.canguan.expense.record.service.ExpenseRecordService;
import cn.abcyun.canguan.expense.support.ApiResponse;
import cn.abcyun.canguan.expense.support.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseRecordService expenseRecordService;

    @GetMapping
    public ApiResponse<PageResult<ExpenseRecordDto>> page(@Valid ExpenseQueryRequest request) {
        return ApiResponse.success(expenseRecordService.page(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ExpenseRecordDto> get(@PathVariable("id") Long id) {
        return ApiResponse.success(expenseRecordService.get(id));
    }

    @GetMapping("/item-options")
    public ApiResponse<java.util.List<String>> itemOptions(@Valid ExpenseItemOptionQueryRequest request) {
        return ApiResponse.success(expenseRecordService.listItemOptions(request));
    }

    @PostMapping
    public ApiResponse<ExpenseRecordDto> create(@RequestBody @Valid ExpenseUpsertRequest request) {
        return ApiResponse.success(expenseRecordService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ExpenseRecordDto> update(@PathVariable("id") Long id, @RequestBody @Valid ExpenseUpsertRequest request) {
        return ApiResponse.success(expenseRecordService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        expenseRecordService.delete(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/history")
    public ApiResponse<java.util.List<ExpenseHistoryDto>> history(@PathVariable("id") Long id) {
        return ApiResponse.success(expenseRecordService.history(id));
    }
}
