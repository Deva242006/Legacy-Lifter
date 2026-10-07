package com.legacylifter.common.repository;

import com.legacylifter.common.entity.ModernizationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModernizationRunRepository extends JpaRepository<ModernizationRun, UUID> {
    Optional<ModernizationRun> findFirstByProjectIdOrderByStartedAtDesc(UUID projectId);
}
