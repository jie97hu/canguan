package cn.abcyun.canguan.expense.report.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ReportDateRangeRequest {

    private LocalDate dateStart;

    private LocalDate dateEnd;

    private Long categoryLevel1Id;

    private Long categoryLevel2Id;
}
