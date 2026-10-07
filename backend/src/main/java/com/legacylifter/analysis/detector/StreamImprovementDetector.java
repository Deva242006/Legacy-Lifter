package com.legacylifter.analysis.detector;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.legacylifter.common.entity.CodeIssue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects legacy stream collector patterns like `.collect(Collectors.toList())`
 * that should be modernized to Java 16+ `.toList()`.
 */
@Component
public final class StreamImprovementDetector implements PatternDetector {

    @Override
    public List<CodeIssue> detect(CompilationUnit cu, String relativeFilePath) {
        List<CodeIssue> issues = new ArrayList<>();

        cu.findAll(MethodCallExpr.class).forEach(call -> {
            if ("collect".equals(call.getNameAsString()) && !call.getArguments().isEmpty()) {
                String argString = call.getArgument(0).toString();
                if (argString.contains("Collectors.toList()")) {
                    call.getRange().ifPresent(range -> {
                        CodeIssue issue = new CodeIssue();
                        issue.setFilePath(relativeFilePath);
                        issue.setLineNumber(range.begin.line);
                        issue.setEndLineNumber(range.end.line);
                        issue.setCategory(CodeIssue.IssueCategory.STREAM_IMPROVEMENTS);
                        issue.setSeverity(CodeIssue.IssueSeverity.LOW);
                        issue.setPatternType(patternType());
                        issue.setDescription("'.collect(Collectors.toList())' can be simplified to Java 16+ '.toList()'.");
                        issue.setOriginalCode(call.toString());
                        issue.setSuggestedFix(".toList()");
                        issue.setAutoFixable(true);
                        issues.add(issue);
                    });
                }
            }
        });

        return issues;
    }

    @Override
    public CodeIssue.IssueCategory category() {
        return CodeIssue.IssueCategory.STREAM_IMPROVEMENTS;
    }

    @Override
    public String patternType() {
        return "COLLECTORS_TO_LIST_SIMPLIFICATION";
    }
}
