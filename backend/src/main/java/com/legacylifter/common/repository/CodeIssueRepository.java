package com.legacylifter.common.repository;

import com.legacylifter.common.entity.CodeIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CodeIssueRepository extends JpaRepository<CodeIssue, UUID> {

    java.util.List<CodeIssue> findByAnalysisRunId(UUID analysisRunId);

    Page<CodeIssue> findByAnalysisRunId(UUID analysisRunId, Pageable pageable);

    Page<CodeIssue> findByAnalysisRunIdAndCategory(UUID analysisRunId,
                                                    CodeIssue.IssueCategory category,
                                                    Pageable pageable);

    Page<CodeIssue> findByAnalysisRunIdAndSeverity(UUID analysisRunId,
                                                    CodeIssue.IssueSeverity severity,
                                                    Pageable pageable);

    @Query("SELECT COUNT(i) FROM CodeIssue i WHERE i.analysisRun.id = :runId AND i.severity = :severity")
    long countByRunIdAndSeverity(UUID runId, CodeIssue.IssueSeverity severity);

    @Query("SELECT COUNT(i) FROM CodeIssue i WHERE i.analysisRun.id = :runId AND i.autoFixable = true")
    long countAutoFixableByRunId(UUID runId);
}
