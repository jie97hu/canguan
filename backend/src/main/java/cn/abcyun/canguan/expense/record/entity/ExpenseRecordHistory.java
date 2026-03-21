package cn.abcyun.canguan.expense.record.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import cn.abcyun.canguan.expense.support.ExpenseActionEnum;
import lombok.Data;

@Data
@Entity
@Table(name = "expense_record_history")
public class ExpenseRecordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "expense_record_id", nullable = false)
    private Long expenseRecordId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ExpenseActionEnum action;

    @Lob
    @Column(name = "before_snapshot")
    private String beforeSnapshot;

    @Lob
    @Column(name = "after_snapshot")
    private String afterSnapshot;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "operator_name", nullable = false, length = 128)
    private String operatorName;

    @Column(name = "operate_time", nullable = false)
    private LocalDateTime operateTime;
}
