package cn.abcyun.canguan.expense.category.entity;

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
@Table(name = "expense_category")
public class ExpenseCategory extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "default_unit", length = 32)
    private String defaultUnit;

    @Column(name = "sort_no", nullable = false)
    private Integer sortNo = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StatusEnum status = StatusEnum.ENABLED;
}
