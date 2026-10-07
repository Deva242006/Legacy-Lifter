package com.legacylifter.analysis.detector;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.legacylifter.common.entity.CodeIssue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Detects legacy Java I/O objects (FileInputStream, FileOutputStream, FileReader, FileWriter)
 * that should be modernized using Java NIO Files API or Virtual Threads.
 */
@Component
public final class BlockingIoDetector implements PatternDetector {

    private static final Set<String> LEGACY_IO_CLASSES = Set.of(
            "FileInputStream", "FileOutputStream", "FileReader", "FileWriter"
    );

    @Override
    public List<CodeIssue> detect(CompilationUnit cu, String relativeFilePath) {
        List<CodeIssue> issues = new ArrayList<>();

        cu.findAll(ObjectCreationExpr.class).forEach(creation -> {
            if (LEGACY_IO_CLASSES.contains(creation.getTypeAsString())) {
                creation.getRange().ifPresent(range -> {
                    CodeIssue issue = new CodeIssue();
                    issue.setFilePath(relativeFilePath);
                    issue.setLineNumber(range.begin.line);
                    issue.setEndLineNumber(range.end.line);
                    issue.setCategory(CodeIssue.IssueCategory.BLOCKING_IO);
                    issue.setSeverity(CodeIssue.IssueSeverity.HIGH);
                    issue.setPatternType(patternType());
                    issue.setDescription("Legacy I/O creation '" + creation.getTypeAsString() + "' detected. Replace with NIO 'java.nio.file.Files.newInputStream()' or 'Files.readString()'.");
                    issue.setOriginalCode(creation.toString());
                    issue.setSuggestedFix("Files.readString(Path.of(...))");
                    issue.setAutoFixable(true);
                    issues.add(issue);
                });
            }
        });

        return issues;
    }

    @Override
    public CodeIssue.IssueCategory category() {
        return CodeIssue.IssueCategory.BLOCKING_IO;
    }

    @Override
    public String patternType() {
        return "LEGACY_BLOCKING_IO";
    }
}
