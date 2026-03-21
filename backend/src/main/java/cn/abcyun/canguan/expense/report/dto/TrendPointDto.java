package cn.abcyun.canguan.expense.report.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TrendPointDto {

    private String label;

    private BigDecimal amount;
}
