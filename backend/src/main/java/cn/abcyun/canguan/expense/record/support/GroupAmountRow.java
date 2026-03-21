package cn.abcyun.canguan.expense.record.support;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class GroupAmountRow {

    private String label;

    private Long id;

    private String name;

    private BigDecimal amount;

    private Long count;
}
