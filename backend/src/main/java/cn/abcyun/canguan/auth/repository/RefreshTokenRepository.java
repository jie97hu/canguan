package cn.abcyun.canguan.auth.repository;

import cn.abcyun.canguan.auth.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByIdAndRevokedFalseAndExpiresAtAfter(String id, LocalDateTime now);
}
