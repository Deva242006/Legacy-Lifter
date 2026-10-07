package com.legacylifter.analysis.controller;

import com.legacylifter.analysis.dto.AnalysisDtos.*;
import com.legacylifter.analysis.service.AnalysisOrchestrator;
import com.legacylifter.common.dto.ApiResponse;
import com.legacylifter.common.entity.AnalysisRun;
import com.legacylifter.common.entity.CodeIssue;
import com.legacylifter.common.entity.Project;
import com.legacylifter.common.exception.LegacyLifterException;
import com.legacylifter.common.repository.AnalysisRunRepository;
import com.legacylifter.common.repository.CodeIssueRepository;
import com.legacylifter.common.repository.ProjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Static Analysis API", description = "Endpoints for project ingestion and AST static code analysis")
public class AnalysisController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    private final ProjectRepository projectRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final CodeIssueRepository codeIssueRepository;
    private final AnalysisOrchestrator analysisOrchestrator;

    public AnalysisController(ProjectRepository projectRepository,
                              AnalysisRunRepository analysisRunRepository,
                              CodeIssueRepository codeIssueRepository,
                              AnalysisOrchestrator analysisOrchestrator) {
        this.projectRepository = projectRepository;
        this.analysisRunRepository = analysisRunRepository;
        this.codeIssueRepository = codeIssueRepository;
        this.analysisOrchestrator = analysisOrchestrator;
    }

    @PostMapping
    @Operation(summary = "Create a new Java project entry for analysis")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(@RequestBody CreateProjectRequest request) {
        Project project = new Project();
        project.setName(request.name());
        project.setDescription(request.description());
        project.setSourceUrl(request.sourceUrl());
        project.setGithubRepoUrl(request.githubRepoUrl());
        project.setJavaVersionDetected("Java 8");
        project.setTargetJavaVersion(request.targetJavaVersion() != null ? request.targetJavaVersion() : "Java 21");
        project.setStatus(Project.ProjectStatus.CREATED);

        Project saved = projectRepository.save(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(ProjectResponse.from(saved)));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a Java codebase as a ZIP archive for analysis")
    public ResponseEntity<ApiResponse<ProjectResponse>> uploadProjectZip(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".zip")) {
            throw LegacyLifterException.badRequest("Please upload a valid non-empty .zip file");
        }

        try {
            Path tempDir = Files.createTempDirectory("legacylifter-zip-");
            try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    Path newPath = tempDir.resolve(entry.getName()).normalize();
                    if (!newPath.startsWith(tempDir)) {
                        throw LegacyLifterException.badRequest("Zip entry security violation: " + entry.getName());
                    }
                    if (entry.isDirectory()) {
                        Files.createDirectories(newPath);
                    } else {
                        Files.createDirectories(newPath.getParent());
                        Files.copy(zis, newPath);
                    }
                }
            }

            Project project = new Project();
            project.setName(name);
            project.setDescription(description != null ? description : "Uploaded ZIP archive");
            project.setSourceUrl(tempDir.toAbsolutePath().toString());
            project.setJavaVersionDetected("Java 8");
            project.setTargetJavaVersion("Java 21");
            project.setStatus(Project.ProjectStatus.CREATED);

            Project saved = projectRepository.save(project);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(ProjectResponse.from(saved)));

        } catch (IOException e) {
            log.error("Failed to extract uploaded zip file: {}", e.getMessage(), e);
            throw LegacyLifterException.internalError("Failed to extract ZIP archive", e);
        }
    }

    @GetMapping
    @Operation(summary = "List all ingested projects")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> listProjects() {
        List<ProjectResponse> projects = projectRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(ProjectResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project details by ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(@PathVariable UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> LegacyLifterException.notFound("Project", id));
        return ResponseEntity.ok(ApiResponse.success(ProjectResponse.from(project)));
    }

    @PostMapping("/{id}/analyze")
    @Operation(summary = "Trigger virtual thread static AST analysis on project source")
    public ResponseEntity<ApiResponse<AnalysisRunResponse>> triggerAnalysis(
            @PathVariable UUID id,
            @RequestParam(value = "path", required = false) String customPath) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> LegacyLifterException.notFound("Project", id));

        String targetPath = customPath != null ? customPath : project.getSourceUrl();
        if (targetPath == null || targetPath.isBlank()) {
            throw LegacyLifterException.badRequest("Project sourceUrl/path is missing.");
        }

        AnalysisRun run = analysisOrchestrator.runAnalysis(id, targetPath);
        return ResponseEntity.ok(ApiResponse.success(AnalysisRunResponse.from(run)));
    }

    @GetMapping("/{id}/analysis")
    @Operation(summary = "Get the latest analysis run for a project")
    public ResponseEntity<ApiResponse<AnalysisRunResponse>> getLatestAnalysis(@PathVariable UUID id) {
        AnalysisRun run = analysisRunRepository.findFirstByProjectIdOrderByStartedAtDesc(id)
                .orElseThrow(() -> LegacyLifterException.notFound("AnalysisRun for project", id));
        return ResponseEntity.ok(ApiResponse.success(AnalysisRunResponse.from(run)));
    }

    @GetMapping("/{id}/issues")
    @Operation(summary = "List detected code issues for a project with optional category/severity filter")
    public ResponseEntity<ApiResponse<Page<CodeIssueResponse>>> getIssues(
            @PathVariable UUID id,
            @RequestParam(required = false) CodeIssue.IssueCategory category,
            @RequestParam(required = false) CodeIssue.IssueSeverity severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        AnalysisRun latestRun = analysisRunRepository.findFirstByProjectIdOrderByStartedAtDesc(id)
                .orElseThrow(() -> LegacyLifterException.notFound("AnalysisRun for project", id));

        Pageable pageable = PageRequest.of(page, size, Sort.by("lineNumber").ascending());
        Page<CodeIssue> issuePage;

        if (category != null) {
            issuePage = codeIssueRepository.findByAnalysisRunIdAndCategory(latestRun.getId(), category, pageable);
        } else if (severity != null) {
            issuePage = codeIssueRepository.findByAnalysisRunIdAndSeverity(latestRun.getId(), severity, pageable);
        } else {
            issuePage = codeIssueRepository.findByAnalysisRunId(latestRun.getId(), pageable);
        }

        Page<CodeIssueResponse> responsePage = issuePage.map(CodeIssueResponse::from);
        return ResponseEntity.ok(ApiResponse.success(responsePage));
    }
}
