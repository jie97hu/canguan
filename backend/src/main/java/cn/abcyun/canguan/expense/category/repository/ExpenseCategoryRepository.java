package cn.abcyun.canguan.expense.category.repository;

import java.util.List;

import cn.abcyun.canguan.expense.category.entity.ExpenseCategory;
import cn.abcyun.canguan.expense.support.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long>, JpaSpecificationExecutor<ExpenseCategory> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    List<ExpenseCategory> findAllByStatusOrderBySortNoAscIdAsc(StatusEnum status);

    List<ExpenseCategory> findAllByParentIdOrderBySortNoAscIdAsc(Long parentId);
}
