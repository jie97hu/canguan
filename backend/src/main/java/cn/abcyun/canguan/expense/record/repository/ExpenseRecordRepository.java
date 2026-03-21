package cn.abcyun.canguan.expense.record.repository;

import java.util.Optional;

import cn.abcyun.canguan.expense.record.entity.ExpenseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExpenseRecordRepository extends JpaRepository<ExpenseRecord, Long>, JpaSpecificationExecutor<ExpenseRecord>, ExpenseRecordRepositoryCustom {

    Optional<ExpenseRecord> findByIdAndDeletedFalse(Long id);
}
