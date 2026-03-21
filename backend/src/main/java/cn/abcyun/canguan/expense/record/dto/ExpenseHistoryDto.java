package cn.abcyun.canguan.expense.record.dto;

import java.time.LocalDateTime;
import java.util.Map;

import cn.abcyun.canguan.expense.support.ExpenseActionEnum;
import lombok.Data;

@Data
public class ExpenseHistoryDto {

    private Long id;

    private Long expenseRecordId;

    private ExpenseActionEnum action;

    private Map<String, Object> beforeSnapshot;

    private Map<String, Object> afterSnapshot;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime operateTime;
}
