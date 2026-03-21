package cn.abcyun.canguan.expense.user.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class UserPasswordRequest {

    @NotBlank(message = "密码不能为空")
    private String password;
}
