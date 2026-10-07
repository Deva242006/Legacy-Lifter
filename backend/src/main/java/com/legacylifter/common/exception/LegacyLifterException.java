package com.legacylifter.common.exception;

/**
 * Base exception for all LegacyLifter-specific errors.
 *
 * <p>Carries an error code for programmatic handling and an HTTP status
 * hint for the global exception handler.</p>
 */
public class LegacyLifterException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    public LegacyLifterException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public LegacyLifterException(String errorCode, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    // ============================================================
    // Factory methods for common error types
    // ============================================================

    public static LegacyLifterException notFound(String entity, Object id) {
        return new LegacyLifterException(
                "NOT_FOUND",
                "%s not found with id: %s".formatted(entity, id),
                404
        );
    }

    public static LegacyLifterException badRequest(String message) {
        return new LegacyLifterException("BAD_REQUEST", message, 400);
    }

    public static LegacyLifterException conflict(String message) {
        return new LegacyLifterException("CONFLICT", message, 409);
    }

    public static LegacyLifterException analysisError(String message, Throwable cause) {
        return new LegacyLifterException("ANALYSIS_ERROR", message, 500, cause);
    }

    public static LegacyLifterException aiError(String message, Throwable cause) {
        return new LegacyLifterException("AI_ERROR", message, 502, cause);
    }

    public static LegacyLifterException internalError(String message, Throwable cause) {
        return new LegacyLifterException("INTERNAL_ERROR", message, 500, cause);
    }

    public static LegacyLifterException githubError(String message, Throwable cause) {
        return new LegacyLifterException("GITHUB_ERROR", message, 502, cause);
    }
}
