package cn.abcyun.canguan.expense.unit.controller;

import javax.validation.Valid;

import cn.abcyun.canguan.expense.support.ApiResponse;
import cn.abcyun.canguan.expense.unit.dto.UnitOptionQueryRequest;
import cn.abcyun.canguan.expense.unit.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    @GetMapping("/options")
    public ApiResponse<java.util.List<String>> options(@Valid UnitOptionQueryRequest request) {
        return ApiResponse.success(unitService.listOptions(request));
    }
}
