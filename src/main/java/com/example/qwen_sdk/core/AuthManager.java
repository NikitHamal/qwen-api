package com.example.qwen_sdk.core;

/**
 * Manages authentication credentials for the Qwen API.
 * This class stores the API key and cookie required for making authenticated requests.
 */
public class AuthManager {
    private final String apiKey;
    private final String cookie;

    /**
     * Constructs an AuthManager with the given API key and cookie.
     *
     * @param apiKey The API key for Qwen services. Must not be null or empty.
     * @param cookie The cookie string for Qwen services. Must not be null or empty.
     * @throws IllegalArgumentException if apiKey or cookie is null or empty.
     */
    public AuthManager(String apiKey, String cookie) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty.");
        }
        // Cookie might be optional for some APIs or use cases, but for Tongyi Qwen chat, it's often needed.
        // If specific endpoints don't need it, this check could be more nuanced or handled by the caller.
        if (cookie == null || cookie.trim().isEmpty()) {
            throw new IllegalArgumentException("Cookie cannot be null or empty.");
        }
        this.apiKey = apiKey;
        this.cookie = cookie;
    }

    /**
     * Gets the Authorization header string.
     *
     * @return The "Bearer {apiKey}" string.
     */
    public String getAuthorizationHeader() {
        return "Bearer " + apiKey;
    }

    /**
     * Gets the Cookie header string.
     *
     * @return The cookie string.
     */
    public String getCookieHeader() {
        return cookie;
    }
}
