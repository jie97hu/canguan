package cn.abcyun.canguan.expense.report.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CategoryBreakdownDto {

    private Long categoryId;

    private String categoryName;

    private BigDecimal amount;

    private BigDecimal ratio;
}
