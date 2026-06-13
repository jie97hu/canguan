package cn.abcyun.canguan.expense.unit.repository;

import java.util.List;
import java.util.Optional;

import cn.abcyun.canguan.expense.support.StatusEnum;
import cn.abcyun.canguan.expense.unit.entity.ExpenseUnit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseUnitRepository extends JpaRepository<ExpenseUnit, Long> {

    Optional<ExpenseUnit> findByName(String name);

    List<ExpenseUnit> findByStatusOrderBySortNoAscIdAsc(StatusEnum status, Pageable pageable);

    List<ExpenseUnit> findByStatusAndNameContainingOrderBySortNoAscIdAsc(StatusEnum status, String keyword, Pageable pageable);
}
