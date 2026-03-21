package cn.abcyun.canguan.expense.store.controller;

import javax.validation.Valid;

import cn.abcyun.canguan.expense.store.dto.StoreDto;
import cn.abcyun.canguan.expense.store.dto.StoreQueryRequest;
import cn.abcyun.canguan.expense.store.dto.StoreStatusRequest;
import cn.abcyun.canguan.expense.store.dto.StoreUpsertRequest;
import cn.abcyun.canguan.expense.store.service.StoreService;
import cn.abcyun.canguan.expense.support.ApiResponse;
import cn.abcyun.canguan.expense.support.PageResult;
import cn.abcyun.canguan.expense.support.StatusEnum;
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
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping
    public ApiResponse<PageResult<StoreDto>> page(@Valid StoreQueryRequest request) {
        return ApiResponse.success(storeService.page(request));
    }

    @GetMapping("/options")
    public ApiResponse<java.util.List<StoreDto>> options() {
        return ApiResponse.success(storeService.options());
    }

    @GetMapping("/{id}")
    public ApiResponse<StoreDto> get(@PathVariable("id") Long id) {
        return ApiResponse.success(storeService.get(id));
    }

    @PostMapping
    public ApiResponse<StoreDto> create(@RequestBody @Valid StoreUpsertRequest request) {
        return ApiResponse.success(storeService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<StoreDto> update(@PathVariable("id") Long id, @RequestBody @Valid StoreUpsertRequest request) {
        return ApiResponse.success(storeService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<StoreDto> changeStatus(@PathVariable("id") Long id, @RequestBody @Valid StoreStatusRequest request) {
        return ApiResponse.success(storeService.changeStatus(id, request.getStatus()));
    }
}
