package cn.abcyun.canguan.expense.record.dto;

import java.time.LocalDate;

import javax.validation.constraints.Min;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class ExpenseQueryRequest {

    private Long storeId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateStart;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateEnd;

    private Long categoryLevel1Id;

    private Long categoryLevel2Id;

    private String itemName;

    private int pageNo = 1;

    private int pageSize = 10;
}
