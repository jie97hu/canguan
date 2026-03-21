package cn.abcyun.canguan.expense.record.support;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ExpenseRecordFilter {

    private Long storeId;

    private LocalDate dateStart;

    private LocalDate dateEnd;

    private Long categoryLevel1Id;

    private Long categoryLevel2Id;

    private String itemName;

    private boolean deleted = false;
}
