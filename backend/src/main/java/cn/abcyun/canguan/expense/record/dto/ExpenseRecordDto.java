package cn.abcyun.canguan.expense.record.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ExpenseRecordDto {

    private Long id;

    private Long storeId;

    private String storeName;

    private LocalDate expenseDate;

    private Long categoryLevel1Id;

    private String categoryLevel1Name;

    private Long categoryLevel2Id;

    private String categoryLevel2Name;

    private String itemName;

    private BigDecimal amount;

    private BigDecimal quantity;

    private String unit;

    private String remark;

    private Long createdBy;

    private String createdByName;

    private Long updatedBy;

    private String updatedByName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
