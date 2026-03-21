package cn.abcyun.canguan.expense.user.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import cn.abcyun.canguan.expense.support.StatusEnum;
import cn.abcyun.canguan.expense.support.UserRoleEnum;
import lombok.Data;

@Data
public class UserUpsertRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String displayName;

    @NotNull(message = "角色不能为空")
    private UserRoleEnum role;

    private Long storeId;

    private StatusEnum status = StatusEnum.ENABLED;
}
