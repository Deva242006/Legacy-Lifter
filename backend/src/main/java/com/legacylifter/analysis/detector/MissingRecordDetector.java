package com.legacylifter.analysis.detector;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.legacylifter.common.entity.CodeIssue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects legacy POJO / DTO classes that qualify for Java Record conversion (immutable data carrier).
 */
@Component
public final class MissingRecordDetector implements PatternDetector {

    @Override
    public List<CodeIssue> detect(CompilationUnit cu, String relativeFilePath) {
        List<CodeIssue> issues = new ArrayList<>();

        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
            if (!clazz.isInterface() && !clazz.isAbstract() && clazz.getExtendedTypes().isEmpty()) {
                // Check if class is non-entity, non-service, non-component
                boolean isCandidate = clazz.getAnnotations().stream().noneMatch(ann ->
                        ann.getNameAsString().equals("Entity") ||
                        ann.getNameAsString().equals("Table") ||
                        ann.getNameAsString().equals("Component") ||
                        ann.getNameAsString().equals("Service") ||
                        ann.getNameAsString().equals("Repository") ||
                        ann.getNameAsString().equals("Controller")
                );

                if (isCandidate) {
                    boolean hasFields = !clazz.getFields().isEmpty();
                    boolean allFieldsPrivate = clazz.getFields().stream().allMatch(f -> f.isPrivate() || f.isFinal());
                    
                    if (hasFields && allFieldsPrivate) {
                        clazz.getRange().ifPresent(range -> {
                            CodeIssue issue = new CodeIssue();
                            issue.setFilePath(relativeFilePath);
                            issue.setLineNumber(range.begin.line);
                            issue.setEndLineNumber(range.end.line);
                            issue.setCategory(CodeIssue.IssueCategory.MISSING_RECORDS);
                            issue.setSeverity(CodeIssue.IssueSeverity.MEDIUM);
                            issue.setPatternType(patternType());
                            issue.setDescription("Class '" + clazz.getNameAsString() + "' appears to be a plain data carrier. Convert to a modern Java Record to eliminate boilerplate.");
                            issue.setOriginalCode("public class " + clazz.getNameAsString() + " { ... }");
                            issue.setSuggestedFix("public record " + clazz.getNameAsString() + "(...) {}");
                            issue.setAutoFixable(true);
                            issues.add(issue);
                        });
                    }
                }
            }
        });

        return issues;
    }

    @Override
    public CodeIssue.IssueCategory category() {
        return CodeIssue.IssueCategory.MISSING_RECORDS;
    }

    @Override
    public String patternType() {
        return "CONVERT_TO_RECORD";
    }
}
