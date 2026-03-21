package cn.abcyun.canguan.expense.support;

import javax.validation.constraints.Min;

import lombok.Data;

@Data
public class PageRequestDto {

    @Min(1)
    private int pageNo = 1;

    @Min(1)
    private int pageSize = 10;
}
