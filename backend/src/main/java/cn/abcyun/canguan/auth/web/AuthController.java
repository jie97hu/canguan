package cn.abcyun.canguan.auth.web;

import cn.abcyun.canguan.auth.security.SecurityUserPrincipal;
import cn.abcyun.canguan.auth.service.AuthService;
import cn.abcyun.canguan.common.api.ApiResponse;
import cn.abcyun.canguan.common.error.BaseErrorCode;
import cn.abcyun.canguan.common.error.BusinessException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Api(tags = "Auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @ApiOperation("登录")
    public ApiResponse<AuthWebModels.LoginResponse> login(@Valid @RequestBody AuthWebModels.LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/refresh")
    @ApiOperation("刷新Token")
    public ApiResponse<AuthWebModels.LoginResponse> refresh(@Valid @RequestBody AuthWebModels.RefreshRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    @PostMapping("/logout")
    @ApiOperation("退出登录")
    public ApiResponse<Void> logout(@Valid @RequestBody AuthWebModels.LogoutRequest request) {
        authService.logout(request);
        return ApiResponse.success();
    }

    @GetMapping("/me")
    @ApiOperation("当前登录人")
    public ApiResponse<AuthWebModels.CurrentUserResponse> me(@AuthenticationPrincipal SecurityUserPrincipal principal) {
        if (principal == null) {
            throw new BusinessException(BaseErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.success(AuthWebModels.CurrentUserResponse.from(principal.toCurrentUser()));
    }
}
