package com.legacylifter.analysis.detector;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.legacylifter.common.entity.CodeIssue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Detects usage of raw types (e.g., {@code List list = new ArrayList()}) without generic parameters.
 */
@Component
public final class RawTypeDetector implements PatternDetector {

    private static final Set<String> GENERIC_COLLECTIONS = Set.of(
            "List", "ArrayList", "LinkedList",
            "Set", "HashSet", "TreeSet", "LinkedHashSet",
            "Map", "HashMap", "TreeMap", "LinkedHashMap", "ConcurrentHashMap",
            "Queue", "Deque", "ArrayDeque", "PriorityQueue"
    );

    @Override
    public List<CodeIssue> detect(CompilationUnit cu, String relativeFilePath) {
        List<CodeIssue> issues = new ArrayList<>();

        cu.findAll(ClassOrInterfaceType.class).forEach(type -> {
            if (GENERIC_COLLECTIONS.contains(type.getNameAsString()) && type.getTypeArguments().isEmpty()) {
                // Ignore if it's inside an import or method declaration return type without context
                type.getRange().ifPresent(range -> {
                    CodeIssue issue = new CodeIssue();
                    issue.setFilePath(relativeFilePath);
                    issue.setLineNumber(range.begin.line);
                    issue.setEndLineNumber(range.end.line);
                    issue.setCategory(CodeIssue.IssueCategory.RAW_TYPES);
                    issue.setSeverity(CodeIssue.IssueSeverity.MEDIUM);
                    issue.setPatternType(patternType());
                    issue.setDescription("Raw collection type '" + type.getNameAsString() + "' used without generic type arguments. Replace with parameterized type (e.g. " + type.getNameAsString() + "<T>).");
                    issue.setOriginalCode(type.toString());
                    issue.setSuggestedFix(type.getNameAsString() + "<Object>");
                    issue.setAutoFixable(true);
                    issues.add(issue);
                });
            }
        });

        return issues;
    }

    @Override
    public CodeIssue.IssueCategory category() {
        return CodeIssue.IssueCategory.RAW_TYPES;
    }

    @Override
    public String patternType() {
        return "RAW_TYPE_USAGE";
    }
}
