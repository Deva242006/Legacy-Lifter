package com.legacylifter.common.repository;

import com.legacylifter.common.entity.AnalysisRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalysisRunRepository extends JpaRepository<AnalysisRun, UUID> {

    List<AnalysisRun> findByProjectIdOrderByStartedAtDesc(UUID projectId);

    Optional<AnalysisRun> findFirstByProjectIdOrderByStartedAtDesc(UUID projectId);
}
