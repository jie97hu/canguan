package cn.abcyun.canguan.expense.support;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageExtra {

    private BigDecimal totalAmount;

    public static PageExtra of(BigDecimal totalAmount) {
        return new PageExtra(totalAmount);
    }
}
