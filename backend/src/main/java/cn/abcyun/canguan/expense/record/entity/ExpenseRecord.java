package cn.abcyun.canguan.expense.record.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import cn.abcyun.canguan.expense.support.BaseAuditEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "expense_record")
public class ExpenseRecord extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "category_level1_id", nullable = false)
    private Long categoryLevel1Id;

    @Column(name = "category_level1_name", nullable = false, length = 128)
    private String categoryLevel1Name;

    @Column(name = "category_level2_id", nullable = false)
    private Long categoryLevel2Id;

    @Column(name = "category_level2_name", nullable = false, length = 128)
    private String categoryLevel2Name;

    @Column(name = "item_name", nullable = false, length = 128)
    private String itemName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(length = 32)
    private String unit;

    @Column(length = 255)
    private String remark;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;

    @Column(nullable = false)
    private Boolean deleted = Boolean.FALSE;

    @Version
    private Long version;
}
