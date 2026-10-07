package com.legacylifter.analysis.detector;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.legacylifter.common.entity.CodeIssue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects abstract classes and interfaces that can be converted to Java Sealed Classes / Interfaces.
 */
@Component
public final class MissingSealedClassDetector implements PatternDetector {

    @Override
    public List<CodeIssue> detect(CompilationUnit cu, String relativeFilePath) {
        List<CodeIssue> issues = new ArrayList<>();

        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
            boolean isSealed = clazz.hasModifier(com.github.javaparser.ast.Modifier.Keyword.SEALED);
            if ((clazz.isInterface() || clazz.isAbstract()) && !isSealed) {
                clazz.getRange().ifPresent(range -> {
                    CodeIssue issue = new CodeIssue();
                    issue.setFilePath(relativeFilePath);
                    issue.setLineNumber(range.begin.line);
                    issue.setEndLineNumber(range.end.line);
                    issue.setCategory(CodeIssue.IssueCategory.MISSING_SEALED_CLASSES);
                    issue.setSeverity(CodeIssue.IssueSeverity.LOW);
                    issue.setPatternType(patternType());
                    issue.setDescription("Abstract type '" + clazz.getNameAsString() + "' can be converted to a sealed interface/class to restrict subclassing and enable exhaustiveness checking in pattern matching.");
                    issue.setOriginalCode((clazz.isInterface() ? "interface " : "abstract class ") + clazz.getNameAsString());
                    issue.setSuggestedFix("public sealed " + (clazz.isInterface() ? "interface " : "class ") + clazz.getNameAsString() + " permits ...");
                    issue.setAutoFixable(false);
                    issues.add(issue);
                });
            }
        });

        return issues;
    }

    @Override
    public CodeIssue.IssueCategory category() {
        return CodeIssue.IssueCategory.MISSING_SEALED_CLASSES;
    }

    @Override
    public String patternType() {
        return "CONVERT_TO_SEALED_TYPE";
    }
}
