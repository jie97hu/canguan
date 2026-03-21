package cn.abcyun.canguan.expense.report.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class StoreComparisonDto {

    private Long storeId;

    private String storeName;

    private BigDecimal amount;

    private Long rankNo;
}
