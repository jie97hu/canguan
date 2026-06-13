package cn.abcyun.canguan.expense.unit.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.Data;

@Data
public class UnitOptionQueryRequest {

    private String keyword;

    @Min(1)
    @Max(200)
    private Integer limit = 200;
}
