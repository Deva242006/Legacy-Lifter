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
 *
 * <p>Pattern: {@link #runModernization} creates a PENDING {@link ModernizationRun} synchronously
 * and returns it immediately to the controller. The heavy LLM/strategy work is delegated to
 * {@link #executeModernizationAsync} which runs on the virtual-thread executor pool.</p>
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

    /**
     * Creates a PENDING {@link ModernizationRun}, persists it, and immediately returns it to the
     * controller. The actual LLM/strategy processing is fired asynchronously via
     * {@link #executeModernizationAsync}.
     *
     * @param projectId     the project to modernize
     * @param analysisRunId the analysis run whose issues will be processed
     * @return the newly-created, PENDING {@link ModernizationRun}
     */
    @Transactional
    public ModernizationRun runModernization(UUID projectId, UUID analysisRunId) {
        log.info("Initiating modernization pipeline for project: {} analysis run: {}", projectId, analysisRunId);

        AnalysisRun analysisRun = analysisRunRepository.findById(analysisRunId)
                .orElseThrow(() -> LegacyLifterException.notFound("AnalysisRun", analysisRunId));

        // Create and persist PENDING run — return this immediately to the HTTP caller
        ModernizationRun run = new ModernizationRun();
        run.setProject(analysisRun.getProject());
        run.setAnalysisRunId(analysisRunId);
        run.setStatus(AnalysisRun.RunStatus.PENDING);
        run.setStartedAt(Instant.now());
        ModernizationRun savedRun = modernizationRunRepository.save(run);

        // Fire the actual LLM/strategy processing asynchronously
        executeModernizationAsync(projectId, savedRun.getId(), analysisRunId);

        return savedRun;
    }

    /**
     * Performs the actual per-issue LLM modernization. Runs asynchronously so the
     * HTTP response for {@link #runModernization} is returned instantly.
     */
    @Async("applicationTaskExecutor")
    @Transactional
    public void executeModernizationAsync(UUID projectId, UUID runId, UUID analysisRunId) {
        log.info("Executing async AI modernization for project {} run {}", projectId, runId);

        ModernizationRun run = modernizationRunRepository.findById(runId).orElse(null);
        if (run == null) {
            log.error("ModernizationRun {} not found for async execution", runId);
            return;
        }

        run.setStatus(AnalysisRun.RunStatus.RUNNING);
        modernizationRunRepository.save(run);

        List<CodeIssue> issues = codeIssueRepository.findByAnalysisRunId(analysisRunId);
        List<ModernizationSuggestion> suggestions = new ArrayList<>();

        for (CodeIssue issue : issues) {
            try {
                var result = llmService.generateModernization(issue);

                ModernizationSuggestion suggestion = new ModernizationSuggestion();
                suggestion.setModernizationRun(run);
                suggestion.setFilePath(issue.getFilePath());
                suggestion.setStrategyType(result.strategyUsed());
                suggestion.setOriginalCode(issue.getOriginalCode() != null ? issue.getOriginalCode() : issue.getDescription());
                suggestion.setModernizedCode(result.modernizedCode());
                suggestion.setExplanation(result.explanation());
                suggestion.setConfidenceScore((float) result.confidenceScore());
                suggestion.setRiskLevel(ModernizationSuggestion.RiskLevel.valueOf(result.riskLevel().toUpperCase()));
                suggestion.setApplied(false);

                suggestions.add(suggestion);
            } catch (Exception e) {
                log.warn("Failed to generate modernization for issue {} in {}: {}", issue.getId(), issue.getFilePath(), e.getMessage());
            }
        }

        modernizationSuggestionRepository.saveAll(suggestions);

        run.setTotalSuggestions(suggestions.size());
        run.setAppliedSuggestions(0);
        run.setStatus(AnalysisRun.RunStatus.COMPLETED);
        run.setCompletedAt(Instant.now());

        ModernizationRun completedRun = modernizationRunRepository.save(run);

        // Publish modernization completed event → triggers test generation pipeline
        eventPublisher.publish(new ModernizationCompletedEvent(
                projectId,
                completedRun.getId(),
                completedRun.getTotalSuggestions()
        ));

        // Push real-time WebSocket notification to subscribed dashboard clients
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/modernization", Map.of(
                "status", "COMPLETED",
                "runId", completedRun.getId().toString(),
                "totalSuggestions", completedRun.getTotalSuggestions()
        ));

        log.info("Completed modernization run {} with {} suggestions for project {}.",
                completedRun.getId(), suggestions.size(), projectId);
    }
}
