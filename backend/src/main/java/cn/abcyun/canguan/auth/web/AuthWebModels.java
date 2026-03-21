package cn.abcyun.canguan.auth.web;

import cn.abcyun.canguan.common.model.CurrentUser;
import cn.abcyun.canguan.common.model.UserRole;
import cn.abcyun.canguan.common.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

public final class AuthWebModels {

    private AuthWebModels() {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshRequest {
        @NotBlank
        private String refreshToken;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogoutRequest {
        @NotBlank
        private String refreshToken;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private long expiresIn;
        private CurrentUserResponse userInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentUserResponse {
        private Long id;
        private String username;
        private String displayName;
        private UserRole role;
        private Long storeId;
        private String storeName;
        private UserStatus status;

        public static CurrentUserResponse from(CurrentUser currentUser) {
            return new CurrentUserResponse(
                    currentUser.getId(),
                    currentUser.getUsername(),
                    currentUser.getDisplayName(),
                    currentUser.getRole(),
                    currentUser.getStoreId(),
                    currentUser.getStoreName(),
                    currentUser.getStatus()
            );
        }
    }
}
