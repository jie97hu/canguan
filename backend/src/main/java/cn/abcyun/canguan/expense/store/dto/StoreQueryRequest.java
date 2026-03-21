package cn.abcyun.canguan.expense.store.dto;

import cn.abcyun.canguan.expense.support.PageRequestDto;
import cn.abcyun.canguan.expense.support.StatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StoreQueryRequest extends PageRequestDto {

    private String keyword;

    private StatusEnum status;
}
