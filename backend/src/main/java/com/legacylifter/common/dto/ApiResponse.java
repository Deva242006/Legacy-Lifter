package com.legacylifter.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standardized API response wrapper for all REST endpoints.
 *
 * <p>Every endpoint returns this structure, ensuring consistent
 * error handling and response parsing on the frontend.</p>
 *
 * @param success  whether the request was successful
 * @param data     the response payload (null on error)
 * @param error    error details (null on success)
 * @param timestamp when the response was generated
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorDetail error,
        Instant timestamp
) {
    /**
     * Create a successful response with data.
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, Instant.now());
    }

    /**
     * Create an error response.
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorDetail(code, message, null), Instant.now());
    }

    /**
     * Create an error response with details.
     */
    public static <T> ApiResponse<T> error(String code, String message, Object details) {
        return new ApiResponse<>(false, null, new ErrorDetail(code, message, details), Instant.now());
    }

    /**
     * Error detail record following RFC 9457 Problem Details.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorDetail(
            String code,
            String message,
            Object details
    ) {}
}
