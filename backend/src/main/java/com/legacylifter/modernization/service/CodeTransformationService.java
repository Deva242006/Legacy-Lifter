package com.legacylifter.modernization.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Service for computing diffs and applying code transformations.
 */
@Service
public class CodeTransformationService {

    /**
     * Generates a unified git-style diff between original legacy code and modernized code.
     */
    public String generateUnifiedDiff(String filePath, String originalCode, String modernizedCode) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- a/").append(filePath).append("\n");
        sb.append("+++ b/").append(filePath).append("\n");
        sb.append("@@ -1,").append(lineCount(originalCode)).append(" +1,").append(lineCount(modernizedCode)).append(" @@\n");

        for (String line : originalCode.split("\\r?\\n")) {
            sb.append("- ").append(line).append("\n");
        }
        for (String line : modernizedCode.split("\\r?\\n")) {
            sb.append("+ ").append(line).append("\n");
        }
        return sb.toString();
    }

    private int lineCount(String str) {
        if (str == null || str.isEmpty()) return 0;
        return str.split("\\r?\\n").length;
    }
}
