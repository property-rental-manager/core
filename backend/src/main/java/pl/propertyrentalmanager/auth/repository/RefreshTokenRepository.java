package pl.propertyrentalmanager.auth.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.propertyrentalmanager.auth.entity.RefreshTokenEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.tokenHash = :tokenHash")
    Optional<RefreshTokenEntity> findByTokenHashWithLock(@Param("tokenHash") String tokenHash);

    List<RefreshTokenEntity> findByTokenFamilyId(UUID tokenFamilyId);

    List<RefreshTokenEntity> findByUserIdAndRevokedAtIsNull(UUID userId);

    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revokedAt = :now, r.revokeReason = :reason WHERE r.user.id = :userId AND r.revokedAt IS NULL")
    void revokeAllByUserId(@Param("userId") UUID userId, @Param("now") OffsetDateTime now, @Param("reason") String reason);

    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revokedAt = :now, r.revokeReason = :reason WHERE r.tokenFamilyId = :familyId AND r.revokedAt IS NULL")
    void revokeFamily(@Param("familyId") UUID familyId, @Param("now") OffsetDateTime now, @Param("reason") String reason);
}
