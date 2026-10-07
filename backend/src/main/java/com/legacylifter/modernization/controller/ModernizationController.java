package com.legacylifter.modernization.controller;

import com.legacylifter.common.dto.ApiResponse;
import com.legacylifter.common.entity.AnalysisRun;
import com.legacylifter.common.entity.ModernizationRun;
import com.legacylifter.common.entity.ModernizationSuggestion;
import com.legacylifter.common.exception.LegacyLifterException;
import com.legacylifter.common.repository.AnalysisRunRepository;
import com.legacylifter.common.repository.ModernizationRunRepository;
import com.legacylifter.common.repository.ModernizationSuggestionRepository;
import com.legacylifter.modernization.dto.ModernizationDtos.*;
import com.legacylifter.modernization.service.ModernizationOrchestrator;
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
@RequestMapping("/api/v1")
@Tag(name = "AI Modernization API", description = "Endpoints for RAG-assisted Java 21+ code modernizations")
public class ModernizationController {

    private final AnalysisRunRepository analysisRunRepository;
    private final ModernizationRunRepository modernizationRunRepository;
    private final ModernizationSuggestionRepository modernizationSuggestionRepository;
    private final ModernizationOrchestrator modernizationOrchestrator;

    public ModernizationController(AnalysisRunRepository analysisRunRepository,
                                   ModernizationRunRepository modernizationRunRepository,
                                   ModernizationSuggestionRepository modernizationSuggestionRepository,
                                   ModernizationOrchestrator modernizationOrchestrator) {
        this.analysisRunRepository = analysisRunRepository;
        this.modernizationRunRepository = modernizationRunRepository;
        this.modernizationSuggestionRepository = modernizationSuggestionRepository;
        this.modernizationOrchestrator = modernizationOrchestrator;
    }

    @PostMapping("/projects/{id}/modernize")
    @Operation(summary = "Trigger AI modernization pipeline over static analysis findings")
    public ResponseEntity<ApiResponse<ModernizationRunResponse>> triggerModernization(@PathVariable UUID id) {
        AnalysisRun latestAnalysis = analysisRunRepository.findFirstByProjectIdOrderByStartedAtDesc(id)
                .orElseThrow(() -> LegacyLifterException.notFound("AnalysisRun for project", id));

        ModernizationRun run = modernizationOrchestrator.runModernization(id, latestAnalysis.getId());
        return ResponseEntity.ok(ApiResponse.success(ModernizationRunResponse.from(run)));
    }

    @GetMapping("/projects/{id}/modernize")
    @Operation(summary = "Get latest modernization run and suggestions")
    public ResponseEntity<ApiResponse<ModernizationRunResponse>> getLatestModernization(@PathVariable UUID id) {
        ModernizationRun run = modernizationRunRepository.findFirstByProjectIdOrderByStartedAtDesc(id)
                .orElseThrow(() -> LegacyLifterException.notFound("ModernizationRun for project", id));
        return ResponseEntity.ok(ApiResponse.success(ModernizationRunResponse.from(run)));
    }

    @GetMapping("/modernization-runs/{runId}/suggestions")
    @Operation(summary = "Get list of modernization suggestions for a run")
    public ResponseEntity<ApiResponse<Page<ModernizationSuggestionResponse>>> getSuggestions(
            @PathVariable UUID runId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ModernizationSuggestion> suggestions = modernizationSuggestionRepository.findByModernizationRunId(runId, pageable);
        return ResponseEntity.ok(ApiResponse.success(suggestions.map(ModernizationSuggestionResponse::from)));
    }

    @PostMapping("/suggestions/{suggestionId}/apply")
    @Operation(summary = "Mark a modernization suggestion as applied")
    public ResponseEntity<ApiResponse<ModernizationSuggestionResponse>> applySuggestion(@PathVariable UUID suggestionId) {
        ModernizationSuggestion suggestion = modernizationSuggestionRepository.findById(suggestionId)
                .orElseThrow(() -> LegacyLifterException.notFound("ModernizationSuggestion", suggestionId));

        suggestion.setApplied(true);
        ModernizationSuggestion updated = modernizationSuggestionRepository.save(suggestion);
        return ResponseEntity.ok(ApiResponse.success(ModernizationSuggestionResponse.from(updated)));
    }
}
