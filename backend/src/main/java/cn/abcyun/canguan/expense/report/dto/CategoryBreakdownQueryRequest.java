package cn.abcyun.canguan.expense.report.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryBreakdownQueryRequest extends OverviewQueryRequest {

    private Integer categoryLevel = 1;
}
