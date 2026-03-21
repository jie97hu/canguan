package cn.abcyun.canguan.expense.store.dto;

import javax.validation.constraints.NotNull;

import cn.abcyun.canguan.expense.support.StatusEnum;
import lombok.Data;

@Data
public class StoreStatusRequest {

    @NotNull(message = "状态不能为空")
    private StatusEnum status;
}
