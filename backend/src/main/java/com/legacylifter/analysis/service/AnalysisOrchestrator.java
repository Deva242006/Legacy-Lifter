package com.legacylifter.analysis.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.legacylifter.analysis.detector.PatternDetector;
import com.legacylifter.common.entity.AnalysisRun;
import com.legacylifter.common.entity.CodeIssue;
import com.legacylifter.common.entity.Project;
import com.legacylifter.common.exception.LegacyLifterException;
import com.legacylifter.common.repository.AnalysisRunRepository;
import com.legacylifter.common.repository.CodeIssueRepository;
import com.legacylifter.common.repository.ProjectRepository;
import com.legacylifter.event.AnalysisCompletedEvent;
import com.legacylifter.event.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Orchestrates deep static analysis on uploaded Java projects.
 *
 * <p>Uses Java 21 Virtual Threads to analyze files concurrently with JavaParser AST detectors.</p>
 */
@Service
public class AnalysisOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AnalysisOrchestrator.class);

    private final ProjectRepository projectRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final CodeIssueRepository codeIssueRepository;
    private final List<PatternDetector> detectors;
    private final SpoonTransformer spoonTransformer;
    private final EventPublisher eventPublisher;
    private final SimpMessagingTemplate messagingTemplate;

    public AnalysisOrchestrator(ProjectRepository projectRepository,
                                AnalysisRunRepository analysisRunRepository,
                                CodeIssueRepository codeIssueRepository,
                                List<PatternDetector> detectors,
                                SpoonTransformer spoonTransformer,
                                EventPublisher eventPublisher,
                                SimpMessagingTemplate messagingTemplate) {
        this.projectRepository = projectRepository;
        this.analysisRunRepository = analysisRunRepository;
        this.codeIssueRepository = codeIssueRepository;
        this.detectors = detectors;
        this.spoonTransformer = spoonTransformer;
        this.eventPublisher = eventPublisher;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Executes analysis asynchronously on virtual threads.
     */
    @Async("applicationTaskExecutor")
    @Transactional
    public AnalysisRun runAnalysis(UUID projectId, String sourceDirPath) {
        log.info("Starting virtual thread static analysis for project ID: {} at path: {}", projectId, sourceDirPath);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> LegacyLifterException.notFound("Project", projectId));

        AnalysisRun run = new AnalysisRun();
        run.setProject(project);
        run.setStatus(AnalysisRun.RunStatus.RUNNING);
        run.setStartedAt(Instant.now());
        run = analysisRunRepository.save(run);

        project.setStatus(Project.ProjectStatus.ANALYZING);
        projectRepository.save(project);

        Path rootPath = Paths.get(sourceDirPath);
        if (!Files.exists(rootPath)) {
            run.setStatus(AnalysisRun.RunStatus.FAILED);
            run.setCompletedAt(Instant.now());
            analysisRunRepository.save(run);
            throw LegacyLifterException.badRequest("Source path does not exist: " + sourceDirPath);
        }

        List<Path> javaFiles;
        try (Stream<Path> walk = Files.walk(rootPath)) {
            javaFiles = walk.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to read directory {}: {}", sourceDirPath, e.getMessage());
            run.setStatus(AnalysisRun.RunStatus.FAILED);
            analysisRunRepository.save(run);
            throw LegacyLifterException.internalError("Error reading source files", e);
        }

        run.setTotalFiles(javaFiles.size());
        analysisRunRepository.save(run);

        List<CodeIssue> collectedIssues = Collections.synchronizedList(new ArrayList<>());
        Map<CodeIssue.IssueCategory, Integer> categoryCounts = new ConcurrentHashMap<>();
        Map<CodeIssue.IssueSeverity, Integer> severityCounts = new ConcurrentHashMap<>();

        final AnalysisRun finalRun = run;

        // Execute parsing concurrently with Java 21 Virtual Threads
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Path file : javaFiles) {
                executor.submit(() -> {
                    String relativePath = rootPath.relativize(file).toString();
                    try {
                        CompilationUnit cu = StaticJavaParser.parse(file);
                        for (PatternDetector detector : detectors) {
                            List<CodeIssue> issues = detector.detect(cu, relativePath);
                            for (CodeIssue issue : issues) {
                                issue.setAnalysisRun(finalRun);
                                collectedIssues.add(issue);

                                categoryCounts.merge(issue.getCategory(), 1, Integer::sum);
                                severityCounts.merge(issue.getSeverity(), 1, Integer::sum);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse file AST: {} - {}", relativePath, e.getMessage());
                    }
                });
            }
        }

        // Save all detected issues
        codeIssueRepository.saveAll(collectedIssues);

        run.setFilesAnalyzed(javaFiles.size());
        run.setIssuesFound(collectedIssues.size());
        run.setStatus(AnalysisRun.RunStatus.COMPLETED);
        run.setCompletedAt(Instant.now());

        Map<String, Object> summary = new HashMap<>();
        summary.put("categoryCounts", categoryCounts);
        summary.put("severityCounts", severityCounts);
        run.setSummary(summary);

        AnalysisRun completedRun = analysisRunRepository.save(run);

        project.setStatus(Project.ProjectStatus.ANALYZED);
        projectRepository.save(project);

        // Publish analysis completed event for tech debt scoring engine & WebSocket updates
        long durationMs = System.currentTimeMillis() - run.getStartedAt().toEpochMilli();
        eventPublisher.publish(new AnalysisCompletedEvent(
                projectId,
                completedRun.getId(),
                completedRun.getIssuesFound(),
                javaFiles.size(),
                durationMs
        ));

        // Push real-time WS update
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/analysis", Map.of(
                "status", "COMPLETED",
                "runId", completedRun.getId(),
                "totalIssues", completedRun.getIssuesFound()
        ));

        log.info("Completed analysis for project {}: {} issues found across {} files", projectId, collectedIssues.size(), javaFiles.size());
        return completedRun;
    }
}
