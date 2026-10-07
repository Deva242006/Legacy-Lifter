package com.legacylifter.common.repository;

import com.legacylifter.common.entity.ScoreTrend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScoreTrendRepository extends JpaRepository<ScoreTrend, UUID> {
    List<ScoreTrend> findByProjectIdAndRecordedAtAfterOrderByRecordedAtAsc(UUID projectId, Instant after);
    List<ScoreTrend> findByProjectIdOrderByRecordedAtAsc(UUID projectId);
}
