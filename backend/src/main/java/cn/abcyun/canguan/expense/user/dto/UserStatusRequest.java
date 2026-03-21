package cn.abcyun.canguan.expense.user.dto;

import javax.validation.constraints.NotNull;

import cn.abcyun.canguan.expense.support.StatusEnum;
import lombok.Data;

@Data
public class UserStatusRequest {

    @NotNull(message = "状态不能为空")
    private StatusEnum status;
}
