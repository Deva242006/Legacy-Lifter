package com.legacylifter.analysis.detector;

import com.github.javaparser.ast.CompilationUnit;
import com.legacylifter.common.entity.CodeIssue;

import java.util.List;

/**
 * Sealed interface for all AST pattern detectors in the LegacyLifter static analysis engine.
 *
 * <p>Each detector inspects a JavaParser {@link CompilationUnit} and identifies outdated Java idioms,
 * performance bottlenecks, or anti-patterns replaceable by modern Java 21+ features.</p>
 */
public sealed interface PatternDetector permits
        RawTypeDetector,
        AnonymousClassDetector,
        OldConcurrencyDetector,
        BlockingIoDetector,
        MissingRecordDetector,
        MissingSealedClassDetector,
        OldSwitchDetector,
        StreamImprovementDetector,
        OptionalMisuseDetector {

    /**
     * Inspects the given compilation unit and returns a list of detected issues.
     *
     * @param cu the parsed Java source file (AST)
     * @param relativeFilePath relative path of the file within the project
     * @return list of detected code issues
     */
    List<CodeIssue> detect(CompilationUnit cu, String relativeFilePath);

    /**
     * Returns the category of issues this detector identifies.
     */
    CodeIssue.IssueCategory category();

    /**
     * Unique identifier for this pattern detector.
     */
    String patternType();
}
