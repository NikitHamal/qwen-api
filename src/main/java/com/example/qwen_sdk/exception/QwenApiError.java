package com.example.qwen_sdk.exception;

/**
 * Custom exception class for errors encountered while interacting with the Qwen API.
 * It can hold details like HTTP status codes and specific error messages from the API.
 */
public class QwenApiError extends RuntimeException {
    private int statusCode;
    private String errorMessage; // Specific error message or details from API response

    /**
     * Constructs a new QwenApiError with a general message, HTTP status code, and detailed error message.
     *
     * @param message A general message describing the error.
     * @param statusCode The HTTP status code associated with the error, if applicable.
     * @param errorMessageDetails Specific error details, often from the API response body.
     */
    public QwenApiError(String message, int statusCode, String errorMessageDetails) {
        super(message);
        this.statusCode = statusCode;
        this.errorMessage = errorMessageDetails;
    }

    /**
     * Constructs a new QwenApiError with a message and a causing throwable.
     * Useful for wrapping lower-level exceptions like IOException.
     *
     * @param message A message describing the error.
     * @param cause The underlying cause of this error.
     */
    public QwenApiError(String message, Throwable cause) {
        super(message, cause);
        // statusCode might not be applicable here, or could be set to a default (e.g., 0 or -1)
    }

    /**
     * Gets the HTTP status code associated with this error.
     *
     * @return The HTTP status code, or 0 if not applicable for this error instance.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the detailed error message, often from the API's response.
     *
     * @return The detailed error message, or null if not set.
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
