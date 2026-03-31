package cn.abcyun.canguan.expense.record.repository;

import java.util.List;
import java.util.Optional;

import cn.abcyun.canguan.expense.record.entity.ExpenseRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRecordRepository extends JpaRepository<ExpenseRecord, Long>, JpaSpecificationExecutor<ExpenseRecord>, ExpenseRecordRepositoryCustom {

    Optional<ExpenseRecord> findByIdAndDeletedFalse(Long id);

    @Query("select distinct record.itemName " +
            "from ExpenseRecord record " +
            "where record.deleted = false " +
            "and (:storeId is null or record.storeId = :storeId) " +
            "and record.categoryLevel1Id = :categoryLevel1Id " +
            "and record.categoryLevel2Id = :categoryLevel2Id " +
            "order by record.itemName asc")
    List<String> findDistinctItemNames(@Param("storeId") Long storeId,
                                       @Param("categoryLevel1Id") Long categoryLevel1Id,
                                       @Param("categoryLevel2Id") Long categoryLevel2Id,
                                       Pageable pageable);
}
