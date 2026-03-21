package cn.abcyun.canguan.expense.support;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS("SUCCESS", "success"),
    UNAUTHORIZED("UNAUTHORIZED", "unauthorized"),
    FORBIDDEN("FORBIDDEN", "forbidden"),
    VALIDATION_ERROR("VALIDATION_ERROR", "validation error"),
    DATA_NOT_FOUND("DATA_NOT_FOUND", "data not found"),
    STORE_DISABLED("STORE_DISABLED", "store disabled"),
    USER_DISABLED("USER_DISABLED", "user disabled"),
    CATEGORY_DISABLED("CATEGORY_DISABLED", "category disabled"),
    EXPENSE_ALREADY_DELETED("EXPENSE_ALREADY_DELETED", "expense already deleted"),
    TOKEN_INVALID("TOKEN_INVALID", "token invalid"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "token expired"),
    REFRESH_TOKEN_INVALID("REFRESH_TOKEN_INVALID", "refresh token invalid"),
    SYSTEM_ERROR("SYSTEM_ERROR", "system error");

    private final String code;

    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
