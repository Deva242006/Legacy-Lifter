package com.legacylifter.common.repository;

import com.legacylifter.common.entity.ModernizationSuggestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ModernizationSuggestionRepository extends JpaRepository<ModernizationSuggestion, UUID> {

    List<ModernizationSuggestion> findByModernizationRunId(UUID runId);

    Page<ModernizationSuggestion> findByModernizationRunId(UUID runId, Pageable pageable);

    List<ModernizationSuggestion> findByModernizationRunIdAndRiskLevel(
            UUID runId, ModernizationSuggestion.RiskLevel riskLevel);

    long countByModernizationRunIdAndApplied(UUID runId, boolean applied);
}
