package cn.abcyun.canguan.expense.record.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import lombok.Data;

@Data
public class ExpenseUpsertRequest {

    @NotNull(message = "分店不能为空")
    private Long storeId;

    @NotNull(message = "支出日期不能为空")
    private LocalDate expenseDate;

    @NotNull(message = "一级分类不能为空")
    private Long categoryLevel1Id;

    @NotNull(message = "二级分类不能为空")
    private Long categoryLevel2Id;

    @NotBlank(message = "品项名称不能为空")
    private String itemName;

    @NotNull(message = "金额不能为空")
    @Positive(message = "金额必须大于0")
    private BigDecimal amount;

    private BigDecimal quantity;

    private String unit;

    private String remark;
}
