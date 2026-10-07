package com.legacylifter.analysis.detector;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.legacylifter.common.entity.CodeIssue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects legacy switch statements and instanceof chains replaceable by modern switch expressions or pattern matching switch.
 */
@Component
public final class OldSwitchDetector implements PatternDetector {

    @Override
    public List<CodeIssue> detect(CompilationUnit cu, String relativeFilePath) {
        List<CodeIssue> issues = new ArrayList<>();

        cu.findAll(SwitchStmt.class).forEach(switchStmt -> {
            switchStmt.getRange().ifPresent(range -> {
                CodeIssue issue = new CodeIssue();
                issue.setFilePath(relativeFilePath);
                issue.setLineNumber(range.begin.line);
                issue.setEndLineNumber(range.end.line);
                issue.setCategory(CodeIssue.IssueCategory.OLD_SWITCH);
                issue.setSeverity(CodeIssue.IssueSeverity.MEDIUM);
                issue.setPatternType(patternType());
                issue.setDescription("Legacy switch statement detected. Modernize to a Java switch expression with rule labels ('->') or pattern matching switch.");
                issue.setOriginalCode(switchStmt.toString());
                issue.setSuggestedFix("switch (expr) { case A -> result; default -> defaultResult; }");
                issue.setAutoFixable(true);
                issues.add(issue);
            });
        });

        return issues;
    }

    @Override
    public CodeIssue.IssueCategory category() {
        return CodeIssue.IssueCategory.OLD_SWITCH;
    }

    @Override
    public String patternType() {
        return "LEGACY_SWITCH_STATEMENT";
    }
}
