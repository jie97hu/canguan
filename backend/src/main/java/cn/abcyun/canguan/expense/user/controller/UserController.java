package cn.abcyun.canguan.expense.user.controller;

import javax.validation.Valid;

import cn.abcyun.canguan.expense.support.ApiResponse;
import cn.abcyun.canguan.expense.support.PageResult;
import cn.abcyun.canguan.expense.support.StatusEnum;
import cn.abcyun.canguan.expense.user.dto.CurrentUserDto;
import cn.abcyun.canguan.expense.user.dto.UserDto;
import cn.abcyun.canguan.expense.user.dto.UserPasswordRequest;
import cn.abcyun.canguan.expense.user.dto.UserQueryRequest;
import cn.abcyun.canguan.expense.user.dto.UserStatusRequest;
import cn.abcyun.canguan.expense.user.dto.UserUpsertRequest;
import cn.abcyun.canguan.expense.user.service.UserService;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<PageResult<UserDto>> page(@Valid UserQueryRequest request) {
        return ApiResponse.success(userService.page(request));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserDto> current() {
        return ApiResponse.success(userService.current());
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> get(@PathVariable("id") Long id) {
        return ApiResponse.success(userService.get(id));
    }

    @PostMapping
    public ApiResponse<UserDto> create(@RequestBody @Valid UserUpsertRequest request) {
        return ApiResponse.success(userService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserDto> update(@PathVariable("id") Long id, @RequestBody @Valid UserUpsertRequest request) {
        return ApiResponse.success(userService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<UserDto> changeStatus(@PathVariable("id") Long id, @RequestBody @Valid UserStatusRequest request) {
        return ApiResponse.success(userService.changeStatus(id, request.getStatus()));
    }

    @PatchMapping("/{id}/password")
    public ApiResponse<UserDto> resetPassword(@PathVariable("id") Long id, @RequestBody @Valid UserPasswordRequest request) {
        return ApiResponse.success(userService.resetPassword(id, request.getPassword()));
    }
}
