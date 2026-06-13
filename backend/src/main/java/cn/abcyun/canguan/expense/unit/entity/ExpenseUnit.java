package cn.abcyun.canguan.expense.unit.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import cn.abcyun.canguan.expense.support.BaseAuditEntity;
import cn.abcyun.canguan.expense.support.StatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "expense_unit")
public class ExpenseUnit extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String name;

    @Column(name = "sort_no", nullable = false)
    private Integer sortNo = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StatusEnum status = StatusEnum.ENABLED;
}
