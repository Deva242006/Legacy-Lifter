package com.legacylifter.common.repository;

import com.legacylifter.common.entity.DebtScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DebtScoreRepository extends JpaRepository<DebtScore, UUID> {
    Optional<DebtScore> findFirstByProjectIdOrderByCalculatedAtDesc(UUID projectId);
}
