package cn.abcyun.canguan.expense.user.dto;

import cn.abcyun.canguan.expense.support.StatusEnum;
import cn.abcyun.canguan.expense.support.UserRoleEnum;
import lombok.Data;

@Data
public class CurrentUserDto {

    private Long id;

    private String username;

    private String displayName;

    private UserRoleEnum role;

    private Long storeId;

    private String storeName;

    private StatusEnum status;
}
