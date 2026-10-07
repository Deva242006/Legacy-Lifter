package com.legacylifter.testgen.controller;

import com.legacylifter.common.dto.ApiResponse;
import com.legacylifter.common.entity.GeneratedTest;
import com.legacylifter.common.entity.ModernizationRun;
import com.legacylifter.common.exception.LegacyLifterException;
import com.legacylifter.common.repository.GeneratedTestRepository;
import com.legacylifter.common.repository.ModernizationRunRepository;
import com.legacylifter.testgen.dto.TestGenDtos.*;
import com.legacylifter.testgen.service.TestGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Smart Test Generation API", description = "Endpoints for generating regression JUnit 5 + AssertJ test suites for modernized code")
public class TestGenController {

    private final GeneratedTestRepository generatedTestRepository;
    private final ModernizationRunRepository modernizationRunRepository;
    private final TestGenerationService testGenerationService;

    public TestGenController(GeneratedTestRepository generatedTestRepository,
                             ModernizationRunRepository modernizationRunRepository,
                             TestGenerationService testGenerationService) {
        this.generatedTestRepository = generatedTestRepository;
        this.modernizationRunRepository = modernizationRunRepository;
        this.testGenerationService = testGenerationService;
    }

    @PostMapping("/{id}/generate-tests")
    @Operation(summary = "Auto-generate JUnit 5 + AssertJ regression tests for latest modernized code")
    public ResponseEntity<ApiResponse<List<GeneratedTestResponse>>> generateTests(@PathVariable UUID id) {
        ModernizationRun latestModernization = modernizationRunRepository.findFirstByProjectIdOrderByStartedAtDesc(id)
                .orElseThrow(() -> LegacyLifterException.notFound("ModernizationRun for project", id));

        List<GeneratedTest> tests = testGenerationService.generateTestsForProject(id, latestModernization.getId());
        List<GeneratedTestResponse> responses = tests.stream().map(GeneratedTestResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}/tests")
    @Operation(summary = "Get list of auto-generated JUnit 5 unit tests for a project")
    public ResponseEntity<ApiResponse<Page<GeneratedTestResponse>>> getTests(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<GeneratedTest> testPage = generatedTestRepository.findByProjectId(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(testPage.map(GeneratedTestResponse::from)));
    }
}
