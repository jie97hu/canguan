package cn.abcyun.canguan.expense.report.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OverviewDto {

    private BigDecimal todayAmount;

    private BigDecimal monthAmount;

    private BigDecimal rangeAmount;

    private Long storeCount;

    private String topStoreName;

    private BigDecimal topStoreAmount;
}
