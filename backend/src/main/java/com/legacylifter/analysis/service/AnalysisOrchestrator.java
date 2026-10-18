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
 *
 * <p>Pattern: {@link #runAnalysis} creates a PENDING {@link AnalysisRun} synchronously and returns
 * it immediately to the caller, then delegates the heavy lifting to {@link #executeAnalysisAsync}
 * which runs on a virtual-thread executor pool without blocking the HTTP response thread.</p>
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
     * Creates a PENDING {@link AnalysisRun}, persists it, and immediately returns it to the
     * controller. The actual AST analysis is fired asynchronously via {@link #executeAnalysisAsync}.
     *
     * @param projectId    the project to analyse
     * @param sourceDirPath filesystem path of the Java source root
     * @return the newly-created, PENDING {@link AnalysisRun}
     */
    @Transactional
    public AnalysisRun runAnalysis(UUID projectId, String sourceDirPath) {
        log.info("Initiating static analysis for project ID: {} at path: {}", projectId, sourceDirPath);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> LegacyLifterException.notFound("Project", projectId));

        // Validate path before creating the run record
        Path rootPath = Paths.get(sourceDirPath);
        if (!Files.exists(rootPath)) {
            throw LegacyLifterException.badRequest("Source path does not exist: " + sourceDirPath);
        }

        // Create and persist a PENDING run — return this to the HTTP caller immediately
        AnalysisRun run = new AnalysisRun();
        run.setProject(project);
        run.setStatus(AnalysisRun.RunStatus.PENDING);
        run.setStartedAt(Instant.now());
        AnalysisRun savedRun = analysisRunRepository.save(run);

        project.setStatus(Project.ProjectStatus.ANALYZING);
        projectRepository.save(project);

        // Fire the heavy processing asynchronously (does NOT block the HTTP response)
        executeAnalysisAsync(projectId, savedRun.getId(), sourceDirPath);

        return savedRun;
    }

    /**
     * Performs the actual virtual-thread AST analysis. Runs asynchronously so the
     * HTTP response for {@link #runAnalysis} is returned instantly.
     */
    @Async("applicationTaskExecutor")
    @Transactional
    public void executeAnalysisAsync(UUID projectId, UUID runId, String sourceDirPath) {
        log.info("Starting virtual-thread AST analysis for project {} run {}", projectId, runId);

        AnalysisRun run = analysisRunRepository.findById(runId)
                .orElse(null);
        if (run == null) {
            log.error("AnalysisRun {} not found for async execution", runId);
            return;
        }

        run.setStatus(AnalysisRun.RunStatus.RUNNING);
        analysisRunRepository.save(run);

        Path rootPath = Paths.get(sourceDirPath);
        List<Path> javaFiles;
        try (Stream<Path> walk = Files.walk(rootPath)) {
            javaFiles = walk.filter(p -> p.toString().endsWith(".java")).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to read directory {}: {}", sourceDirPath, e.getMessage());
            run.setStatus(AnalysisRun.RunStatus.FAILED);
            run.setCompletedAt(Instant.now());
            analysisRunRepository.save(run);
            return;
        }

        run.setTotalFiles(javaFiles.size());
        analysisRunRepository.save(run);

        List<CodeIssue> collectedIssues = Collections.synchronizedList(new ArrayList<>());
        Map<CodeIssue.IssueCategory, Integer> categoryCounts = new ConcurrentHashMap<>();
        Map<CodeIssue.IssueSeverity, Integer> severityCounts = new ConcurrentHashMap<>();

        final AnalysisRun finalRun = run;

        // Execute parsing concurrently with Java 21 Virtual Threads (JEP 444)
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
        } // Virtual thread executor is auto-closed here, awaiting all submitted tasks

        // Save all detected issues in a batch
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

        Project project = completedRun.getProject();
        project.setStatus(Project.ProjectStatus.ANALYZED);
        projectRepository.save(project);

        // Publish domain event → triggers debt scoring + modernization pipeline
        long durationMs = System.currentTimeMillis() - run.getStartedAt().toEpochMilli();
        eventPublisher.publish(new AnalysisCompletedEvent(
                projectId,
                completedRun.getId(),
                completedRun.getIssuesFound(),
                javaFiles.size(),
                durationMs
        ));

        // Push real-time WebSocket notification to subscribed dashboard clients
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/analysis", Map.of(
                "status", "COMPLETED",
                "runId", completedRun.getId().toString(),
                "totalIssues", completedRun.getIssuesFound(),
                "totalFiles", completedRun.getFilesAnalyzed()
        ));

        log.info("Completed analysis for project {}: {} issues found across {} files in {}ms",
                projectId, collectedIssues.size(), javaFiles.size(), durationMs);
    }
}
