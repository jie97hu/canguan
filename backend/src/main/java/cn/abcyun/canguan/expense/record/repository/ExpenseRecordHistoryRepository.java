package cn.abcyun.canguan.expense.record.repository;

import java.util.List;

import cn.abcyun.canguan.expense.record.entity.ExpenseRecordHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRecordHistoryRepository extends JpaRepository<ExpenseRecordHistory, Long> {

    List<ExpenseRecordHistory> findByExpenseRecordIdOrderByOperateTimeDescIdDesc(Long expenseRecordId);
}
