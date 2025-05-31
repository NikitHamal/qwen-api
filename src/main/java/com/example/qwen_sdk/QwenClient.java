package com.example.qwen_sdk;

import com.example.qwen_sdk.core.AuthManager;
import com.example.qwen_sdk.resources.ChatCompletion;
import com.example.qwen_sdk.resources.FileUpload;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main client class for interacting with the Qwen API.
 * This class initializes shared components like {@link AuthManager}, {@link OkHttpClient}, and {@link Gson},
 * and provides access to specific API resource handlers like {@link ChatCompletion} and {@link FileUpload}.
 */
public class QwenClient {
    private final AuthManager authManager;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String baseUrl;
    private final int timeout; // in seconds
    private final Logger logger;
    private final String defaultModel;

    // Resource handlers
    private final ChatCompletion chatCompletionResource;
    private final FileUpload fileUploadResource;

    /**
     * Constructs a new QwenClient with detailed configuration.
     *
     * @param apiKey Your Qwen API key.
     * @param cookie Your Qwen API cookie.
     * @param baseUrl The base URL for the Qwen API. Defaults to "https://chat.qwen.ai" if null or empty.
     * @param timeout The connection, read, and write timeout in seconds for HTTP requests. Defaults to 600 if non-positive.
     * @param loggingLevelStr The logging level string (e.g., "INFO", "DEBUG", "WARNING"). Defaults to "INFO".
     * @param saveLogs (Currently a placeholder) A flag indicating if logs should be saved.
     * @param defaultModel The default model to be used for requests if not specified, e.g., "qwen-turbo".
     * @throws IllegalArgumentException if apiKey or cookie is null or empty.
     */
    public QwenClient(String apiKey, String cookie, String baseUrl, int timeout, String loggingLevelStr, boolean saveLogs, String defaultModel) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty.");
        }
        if (cookie == null || cookie.trim().isEmpty()) {
            // This check might be relaxed based on specific API endpoint requirements.
            throw new IllegalArgumentException("Cookie cannot be null or empty.");
        }
        this.authManager = new AuthManager(apiKey, cookie);
        this.baseUrl = (baseUrl == null || baseUrl.trim().isEmpty()) ? "https://chat.qwen.ai" : baseUrl;
        this.timeout = (timeout <= 0) ? 600 : timeout; // Default to 10 minutes
        this.defaultModel = (defaultModel == null || defaultModel.trim().isEmpty()) ? "qwen-turbo" : defaultModel;

        this.gson = new Gson();

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(this.timeout, TimeUnit.SECONDS)
                .readTimeout(this.timeout, TimeUnit.SECONDS)
                .writeTimeout(this.timeout, TimeUnit.SECONDS)
                .build();

        this.logger = Logger.getLogger(QwenClient.class.getName());
        try {
            Level level = (loggingLevelStr == null || loggingLevelStr.trim().isEmpty()) ? Level.INFO : Level.parse(loggingLevelStr.toUpperCase());
            this.logger.setLevel(level);
        } catch (IllegalArgumentException e) {
            this.logger.setLevel(Level.INFO); // Default level on parse error
            this.logger.warning("Invalid logging level string: " + loggingLevelStr + ". Defaulting to INFO. Error: " + e.getMessage());
        }

        this.logger.info("QwenClient initialized. Base URL: " + this.baseUrl +
                           ", Timeout: " + this.timeout + "s, Logging Level: " +
                           this.logger.getLevel() + ", Default Model: " + this.defaultModel);

        if (saveLogs) {
            this.logger.info("Log saving enabled (actual file saving implementation to be added if needed).");
            // Example: Add FileHandler here for persistent logging
        }

        // Initialize resource handlers
        this.chatCompletionResource = new ChatCompletion(this);
        this.fileUploadResource = new FileUpload(this);
    }

    /**
     * Constructs a new QwenClient with default settings for baseUrl, timeout, logging, and a specific default model.
     * Default Base URL: "https://chat.qwen.ai"
     * Default Timeout: 600 seconds
     * Default Logging Level: "INFO"
     *
     * @param apiKey Your Qwen API key.
     * @param cookie Your Qwen API cookie.
     * @param defaultModel The default model to be used (e.g., "qwen-max", "qwen-turbo").
     */
    public QwenClient(String apiKey, String cookie, String defaultModel) {
        this(apiKey, cookie, "https://chat.qwen.ai", 600, "INFO", false, defaultModel);
    }

    /**
     * Constructs a new QwenClient with default settings for baseUrl, timeout, logging, and "qwen-turbo" as default model.
     *
     * @param apiKey Your Qwen API key.
     * @param cookie Your Qwen API cookie.
     */
    public QwenClient(String apiKey, String cookie) {
        this(apiKey, cookie, "https://chat.qwen.ai", 600, "INFO", false, "qwen-turbo");
    }

    /**
     * Gets the {@link AuthManager} instance used by this client.
     * @return The AuthManager.
     */
    public AuthManager getAuthManager() {
        return authManager;
    }

    /**
     * Gets the {@link OkHttpClient} instance used by this client.
     * @return The OkHttpClient.
     */
    public OkHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Gets the {@link Gson} instance used for JSON serialization/deserialization.
     * @return The Gson instance.
     */
    public Gson getGson() {
        return gson;
    }

    /**
     * Gets the base URL for the Qwen API.
     * @return The base URL.
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Gets the configured timeout for HTTP requests in seconds.
     * @return The timeout value.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Gets the {@link Logger} instance used by this client.
     * @return The Logger.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Gets the configured default model name.
     * @return The default model name.
     */
    public String getDefaultModel() {
        return defaultModel;
    }

    /**
     * Gets the handler for Chat Completion API operations.
     * @return The {@link ChatCompletion} resource handler.
     */
    public ChatCompletion getChatCompletion() {
        return chatCompletionResource;
    }

    /**
     * Gets the handler for File Upload API operations.
     * @return The {@link FileUpload} resource handler.
     */
    public FileUpload getFileUpload() {
        return fileUploadResource;
    }
}
