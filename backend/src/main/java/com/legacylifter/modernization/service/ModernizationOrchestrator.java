package com.legacylifter.modernization.service;

import com.legacylifter.common.entity.AnalysisRun;
import com.legacylifter.common.entity.CodeIssue;
import com.legacylifter.common.entity.ModernizationRun;
import com.legacylifter.common.entity.ModernizationSuggestion;
import com.legacylifter.common.exception.LegacyLifterException;
import com.legacylifter.common.repository.AnalysisRunRepository;
import com.legacylifter.common.repository.CodeIssueRepository;
import com.legacylifter.common.repository.ModernizationRunRepository;
import com.legacylifter.common.repository.ModernizationSuggestionRepository;
import com.legacylifter.event.EventPublisher;
import com.legacylifter.event.ModernizationCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates AI modernization workflow across static analysis issues.
 */
@Service
public class ModernizationOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ModernizationOrchestrator.class);

    private final AnalysisRunRepository analysisRunRepository;
    private final CodeIssueRepository codeIssueRepository;
    private final ModernizationRunRepository modernizationRunRepository;
    private final ModernizationSuggestionRepository modernizationSuggestionRepository;
    private final LlmService llmService;
    private final CodeTransformationService codeTransformationService;
    private final EventPublisher eventPublisher;
    private final SimpMessagingTemplate messagingTemplate;

    public ModernizationOrchestrator(AnalysisRunRepository analysisRunRepository,
                                     CodeIssueRepository codeIssueRepository,
                                     ModernizationRunRepository modernizationRunRepository,
                                     ModernizationSuggestionRepository modernizationSuggestionRepository,
                                     LlmService llmService,
                                     CodeTransformationService codeTransformationService,
                                     EventPublisher eventPublisher,
                                     SimpMessagingTemplate messagingTemplate) {
        this.analysisRunRepository = analysisRunRepository;
        this.codeIssueRepository = codeIssueRepository;
        this.modernizationRunRepository = modernizationRunRepository;
        this.modernizationSuggestionRepository = modernizationSuggestionRepository;
        this.llmService = llmService;
        this.codeTransformationService = codeTransformationService;
        this.eventPublisher = eventPublisher;
        this.messagingTemplate = messagingTemplate;
    }

    @Async("applicationTaskExecutor")
    @Transactional
    public ModernizationRun runModernization(UUID projectId, UUID analysisRunId) {
        log.info("Starting modernization execution for project: {} run: {}", projectId, analysisRunId);

        AnalysisRun analysisRun = analysisRunRepository.findById(analysisRunId)
                .orElseThrow(() -> LegacyLifterException.notFound("AnalysisRun", analysisRunId));

        ModernizationRun run = new ModernizationRun();
        run.setProject(analysisRun.getProject());
        run.setAnalysisRunId(analysisRunId);
        run.setStatus(AnalysisRun.RunStatus.RUNNING);
        run.setStartedAt(Instant.now());
        run = modernizationRunRepository.save(run);

        List<CodeIssue> issues = codeIssueRepository.findByAnalysisRunId(analysisRunId);
        List<ModernizationSuggestion> suggestions = new ArrayList<>();

        for (CodeIssue issue : issues) {
            var result = llmService.generateModernization(issue);

            ModernizationSuggestion suggestion = new ModernizationSuggestion();
            suggestion.setModernizationRun(run);
            suggestion.setFilePath(issue.getFilePath());
            suggestion.setStrategyType(result.strategyUsed());
            suggestion.setOriginalCode(issue.getOriginalCode() != null ? issue.getOriginalCode() : issue.getDescription());
            suggestion.setModernizedCode(result.modernizedCode());
            suggestion.setExplanation(result.explanation());
            suggestion.setConfidenceScore(result.confidenceScore());
            suggestion.setRiskLevel(result.riskLevel());
            suggestion.setApplied(false);

            suggestions.add(suggestion);
        }

        modernizationSuggestionRepository.saveAll(suggestions);

        run.setTotalSuggestions(suggestions.size());
        run.setAppliedSuggestions(0);
        run.setStatus(AnalysisRun.RunStatus.COMPLETED);
        run.setCompletedAt(Instant.now());

        ModernizationRun completedRun = modernizationRunRepository.save(run);

        // Publish modernization completed event
        eventPublisher.publish(new ModernizationCompletedEvent(
                projectId,
                completedRun.getId(),
                completedRun.getTotalSuggestions()
        ));

        // Push real-time WS update
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/modernization", Map.of(
                "status", "COMPLETED",
                "runId", completedRun.getId(),
                "totalSuggestions", completedRun.getTotalSuggestions()
        ));

        log.info("Completed modernization run {} with {} suggestions generated.", completedRun.getId(), suggestions.size());
        return completedRun;
    }
}
