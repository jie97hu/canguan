package cn.abcyun.canguan.expense.report.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ItemRankingDto {

    private String itemName;

    private BigDecimal amount;

    private Long rankNo;
}
