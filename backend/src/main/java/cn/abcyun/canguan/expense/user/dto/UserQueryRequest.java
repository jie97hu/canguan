package cn.abcyun.canguan.expense.user.dto;

import cn.abcyun.canguan.expense.support.PageRequestDto;
import cn.abcyun.canguan.expense.support.StatusEnum;
import cn.abcyun.canguan.expense.support.UserRoleEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageRequestDto {

    private String keyword;

    private UserRoleEnum role;

    private StatusEnum status;

    private Long storeId;
}
