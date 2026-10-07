package com.legacylifter.event;

import com.legacylifter.modernization.service.ModernizationOrchestrator;
import com.legacylifter.scoring.service.DebtScoringService;
import com.legacylifter.testgen.service.TestGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event-driven pipeline listener that seamlessly chains:
 * AnalysisCompleted -> DebtScoring & AI Modernization -> JUnit 5 Test Generation.
 */
@Component
public class PipelineEventListener {

    private static final Logger log = LoggerFactory.getLogger(PipelineEventListener.class);

    private final DebtScoringService debtScoringService;
    private final ModernizationOrchestrator modernizationOrchestrator;
    private final TestGenerationService testGenerationService;

    public PipelineEventListener(DebtScoringService debtScoringService,
                                 ModernizationOrchestrator modernizationOrchestrator,
                                 TestGenerationService testGenerationService) {
        this.debtScoringService = debtScoringService;
        this.modernizationOrchestrator = modernizationOrchestrator;
        this.testGenerationService = testGenerationService;
    }

    @Async("applicationTaskExecutor")
    @EventListener
    public void handleAnalysisCompleted(AnalysisCompletedEvent event) {
        log.info("Event Received: AnalysisCompleted for project {}. Triggering tech debt scoring and AI modernization...", event.projectId());
        
        // 1. Compute technical debt score
        debtScoringService.calculateScore(event.projectId(), event.analysisRunId());

        // 2. Trigger AI modernization pipeline
        modernizationOrchestrator.runModernization(event.projectId(), event.analysisRunId());
    }

    @Async("applicationTaskExecutor")
    @EventListener
    public void handleModernizationCompleted(ModernizationCompletedEvent event) {
        log.info("Event Received: ModernizationCompleted for project {}. Triggering JUnit 5 test generation...", event.projectId());

        // 3. Generate unit tests for modernized code
        testGenerationService.generateTestsForProject(event.projectId(), event.modernizationRunId());
    }
}
