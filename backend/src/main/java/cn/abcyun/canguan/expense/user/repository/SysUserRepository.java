package cn.abcyun.canguan.expense.user.repository;

import java.util.List;

import cn.abcyun.canguan.expense.support.StatusEnum;
import cn.abcyun.canguan.expense.support.UserRoleEnum;
import cn.abcyun.canguan.expense.user.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SysUserRepository extends JpaRepository<SysUser, Long>, JpaSpecificationExecutor<SysUser> {

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByUsername(String username);

    List<SysUser> findAllByStatusOrderByIdAsc(StatusEnum status);

    long countByRoleAndStatus(UserRoleEnum role, StatusEnum status);

    SysUser findByUsername(String username);
}
