package cn.abcyun.canguan.expense.record.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class ExpenseItemOptionQueryRequest {

    private Long storeId;

    @NotNull(message = "一级分类不能为空")
    private Long categoryLevel1Id;

    @NotNull(message = "二级分类不能为空")
    private Long categoryLevel2Id;

    @Min(1)
    @Max(200)
    private Integer limit = 100;
}
