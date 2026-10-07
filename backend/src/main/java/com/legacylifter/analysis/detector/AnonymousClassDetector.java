package com.legacylifter.analysis.detector;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.legacylifter.common.entity.CodeIssue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects anonymous inner classes implementing SAM interfaces that can be converted into lambdas.
 */
@Component
public final class AnonymousClassDetector implements PatternDetector {

    @Override
    public List<CodeIssue> detect(CompilationUnit cu, String relativeFilePath) {
        List<CodeIssue> issues = new ArrayList<>();

        cu.findAll(ObjectCreationExpr.class).forEach(creation -> {
            if (creation.getAnonymousClassBody().isPresent()) {
                var body = creation.getAnonymousClassBody().get();
                // If the anonymous class body has exactly 1 method, it's a prime lambda candidate
                long methodCount = body.stream().filter(node -> node.getClass().getSimpleName().equals("MethodDeclaration")).count();
                if (methodCount == 1) {
                    creation.getRange().ifPresent(range -> {
                        CodeIssue issue = new CodeIssue();
                        issue.setFilePath(relativeFilePath);
                        issue.setLineNumber(range.begin.line);
                        issue.setEndLineNumber(range.end.line);
                        issue.setCategory(CodeIssue.IssueCategory.ANONYMOUS_CLASSES);
                        issue.setSeverity(CodeIssue.IssueSeverity.LOW);
                        issue.setPatternType(patternType());
                        issue.setDescription("Anonymous class instantiation for '" + creation.getTypeAsString() + "' can be converted to a modern Java lambda expression or method reference.");
                        issue.setOriginalCode(creation.toString());
                        issue.setSuggestedFix("() -> { /* method body */ }");
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
        return CodeIssue.IssueCategory.ANONYMOUS_CLASSES;
    }

    @Override
    public String patternType() {
        return "ANONYMOUS_CLASS_TO_LAMBDA";
    }
}
