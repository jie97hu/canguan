package cn.abcyun.canguan.expense.store.repository;

import java.util.List;

import cn.abcyun.canguan.expense.store.entity.Store;
import cn.abcyun.canguan.expense.support.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StoreRepository extends JpaRepository<Store, Long>, JpaSpecificationExecutor<Store> {

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByCode(String code);

    List<Store> findAllByStatusOrderByIdAsc(StatusEnum status);
}
