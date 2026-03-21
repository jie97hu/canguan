package cn.abcyun.canguan.expense.store.dto;

import javax.validation.constraints.NotBlank;

import cn.abcyun.canguan.expense.support.StatusEnum;
import lombok.Data;

@Data
public class StoreUpsertRequest {

    @NotBlank(message = "分店名称不能为空")
    private String name;

    @NotBlank(message = "分店编码不能为空")
    private String code;

    private StatusEnum status = StatusEnum.ENABLED;
}
