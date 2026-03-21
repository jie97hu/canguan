package cn.abcyun.canguan.common.error;

import org.springframework.http.HttpStatus;

public enum BaseErrorCode implements ErrorCode {
    SUCCESS("SUCCESS", "success", HttpStatus.OK),
    UNAUTHORIZED("UNAUTHORIZED", "未登录或账号密码错误", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "无权访问", HttpStatus.FORBIDDEN),
    VALIDATION_ERROR("VALIDATION_ERROR", "参数校验失败", HttpStatus.BAD_REQUEST),
    DATA_NOT_FOUND("DATA_NOT_FOUND", "数据不存在", HttpStatus.NOT_FOUND),
    STORE_DISABLED("STORE_DISABLED", "分店已停用", HttpStatus.BAD_REQUEST),
    USER_DISABLED("USER_DISABLED", "账号已停用", HttpStatus.FORBIDDEN),
    CATEGORY_DISABLED("CATEGORY_DISABLED", "分类已停用", HttpStatus.BAD_REQUEST),
    EXPENSE_ALREADY_DELETED("EXPENSE_ALREADY_DELETED", "支出已删除", HttpStatus.BAD_REQUEST),
    TOKEN_INVALID("TOKEN_INVALID", "Token无效", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "Token已过期", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID("REFRESH_TOKEN_INVALID", "刷新令牌无效", HttpStatus.UNAUTHORIZED),
    SYSTEM_ERROR("SYSTEM_ERROR", "系统异常", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    BaseErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
