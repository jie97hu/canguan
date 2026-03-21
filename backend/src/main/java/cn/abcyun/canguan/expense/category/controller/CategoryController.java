package cn.abcyun.canguan.expense.category.controller;

import javax.validation.Valid;

import cn.abcyun.canguan.expense.category.dto.CategoryNodeDto;
import cn.abcyun.canguan.expense.category.dto.CategoryQueryRequest;
import cn.abcyun.canguan.expense.category.dto.CategoryStatusRequest;
import cn.abcyun.canguan.expense.category.dto.CategoryUpsertRequest;
import cn.abcyun.canguan.expense.category.service.CategoryService;
import cn.abcyun.canguan.expense.support.ApiResponse;
import cn.abcyun.canguan.expense.support.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/tree")
    public ApiResponse<java.util.List<CategoryNodeDto>> tree() {
        return ApiResponse.success(categoryService.tree());
    }

    @GetMapping
    public ApiResponse<PageResult<CategoryNodeDto>> page(@Valid CategoryQueryRequest request) {
        return ApiResponse.success(categoryService.page(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryNodeDto> get(@PathVariable("id") Long id) {
        return ApiResponse.success(categoryService.get(id));
    }

    @PostMapping
    public ApiResponse<CategoryNodeDto> create(@RequestBody @Valid CategoryUpsertRequest request) {
        return ApiResponse.success(categoryService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryNodeDto> update(@PathVariable("id") Long id, @RequestBody @Valid CategoryUpsertRequest request) {
        return ApiResponse.success(categoryService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<CategoryNodeDto> changeStatus(@PathVariable("id") Long id, @RequestBody @Valid CategoryStatusRequest request) {
        return ApiResponse.success(categoryService.changeStatus(id, request.getStatus()));
    }
}
