package com.legacylifter.analysis.detector;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.legacylifter.common.entity.CodeIssue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects unsafe or unidiomatic usage of Optional (e.g. calling .get() directly or field/parameter Optional usage).
 */
@Component
public final class OptionalMisuseDetector implements PatternDetector {

    @Override
    public List<CodeIssue> detect(CompilationUnit cu, String relativeFilePath) {
        List<CodeIssue> issues = new ArrayList<>();

        cu.findAll(MethodCallExpr.class).forEach(call -> {
            if ("get".equals(call.getNameAsString()) && call.getScope().isPresent()) {
                String scopeStr = call.getScope().get().toString();
                // Heuristic check for optional scope names or getters returning Optional
                if (scopeStr.toLowerCase().contains("opt") || scopeStr.endsWith("Optional")) {
                    call.getRange().ifPresent(range -> {
                        CodeIssue issue = new CodeIssue();
                        issue.setFilePath(relativeFilePath);
                        issue.setLineNumber(range.begin.line);
                        issue.setEndLineNumber(range.end.line);
                        issue.setCategory(CodeIssue.IssueCategory.OPTIONAL_MISUSE);
                        issue.setSeverity(CodeIssue.IssueSeverity.MEDIUM);
                        issue.setPatternType(patternType());
                        issue.setDescription("Direct call to 'Optional.get()' is unsafe. Modernize using '.orElseThrow()', '.ifPresent()', or '.map()'.");
                        issue.setOriginalCode(call.toString());
                        issue.setSuggestedFix(scopeStr + ".orElseThrow()");
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
        return CodeIssue.IssueCategory.OPTIONAL_MISUSE;
    }

    @Override
    public String patternType() {
        return "DIRECT_OPTIONAL_GET";
    }
}
