package com.legacylifter.scoring.controller;

import com.legacylifter.common.dto.ApiResponse;
import com.legacylifter.common.entity.AnalysisRun;
import com.legacylifter.common.entity.DebtScore;
import com.legacylifter.common.entity.ScoreTrend;
import com.legacylifter.common.exception.LegacyLifterException;
import com.legacylifter.common.repository.AnalysisRunRepository;
import com.legacylifter.common.repository.DebtScoreRepository;
import com.legacylifter.common.repository.ScoreTrendRepository;
import com.legacylifter.scoring.dto.ScoringDtos.*;
import com.legacylifter.scoring.service.DebtScoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Technical Debt Scoring API", description = "Endpoints for project debt scoring breakdown and historical trend analysis")
public class ScoringController {

    private final DebtScoreRepository debtScoreRepository;
    private final ScoreTrendRepository scoreTrendRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final DebtScoringService debtScoringService;

    public ScoringController(DebtScoreRepository debtScoreRepository,
                             ScoreTrendRepository scoreTrendRepository,
                             AnalysisRunRepository analysisRunRepository,
                             DebtScoringService debtScoringService) {
        this.debtScoreRepository = debtScoreRepository;
        this.scoreTrendRepository = scoreTrendRepository;
        this.analysisRunRepository = analysisRunRepository;
        this.debtScoringService = debtScoringService;
    }

    @PostMapping("/{id}/calculate-score")
    @Operation(summary = "Calculate Technical Debt score for latest static analysis run")
    public ResponseEntity<ApiResponse<DebtScoreResponse>> calculateScore(@PathVariable UUID id) {
        AnalysisRun latestAnalysis = analysisRunRepository.findFirstByProjectIdOrderByStartedAtDesc(id)
                .orElseThrow(() -> LegacyLifterException.notFound("AnalysisRun for project", id));

        DebtScore score = debtScoringService.calculateScore(id, latestAnalysis.getId());
        return ResponseEntity.ok(ApiResponse.success(DebtScoreResponse.from(score)));
    }

    @GetMapping("/{id}/score")
    @Operation(summary = "Get latest Technical Debt Score for project")
    public ResponseEntity<ApiResponse<DebtScoreResponse>> getLatestScore(@PathVariable UUID id) {
        DebtScore score = debtScoreRepository.findFirstByProjectIdOrderByCalculatedAtDesc(id)
                .orElseGet(() -> {
                    AnalysisRun latestAnalysis = analysisRunRepository.findFirstByProjectIdOrderByStartedAtDesc(id)
                            .orElseThrow(() -> LegacyLifterException.notFound("AnalysisRun for project", id));
                    return debtScoringService.calculateScore(id, latestAnalysis.getId());
                });
        return ResponseEntity.ok(ApiResponse.success(DebtScoreResponse.from(score)));
    }

    @GetMapping("/{id}/score-trend")
    @Operation(summary = "Get historical score trends for dashboard line chart visualization")
    public ResponseEntity<ApiResponse<List<ScoreTrendResponse>>> getScoreTrends(@PathVariable UUID id) {
        List<ScoreTrendResponse> trends = scoreTrendRepository.findByProjectIdOrderByRecordedAtAsc(id)
                .stream()
                .map(ScoreTrendResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(trends));
    }
}
