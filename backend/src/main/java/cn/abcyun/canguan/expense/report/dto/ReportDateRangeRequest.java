package cn.abcyun.canguan.expense.report.dto;

import java.time.LocalDate;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class ReportDateRangeRequest {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateStart;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateEnd;

    private Long categoryLevel1Id;

    private Long categoryLevel2Id;
}
