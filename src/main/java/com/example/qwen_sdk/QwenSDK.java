package com.example.qwen_sdk;

// Common imports from QwenClient and other files
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import okhttp3.MultipartBody; // Added for FileUpload

import java.io.BufferedReader;
import java.io.File; // Added for FileUpload
import java.io.IOException;
import java.nio.file.Files; // Added for FileUpload
import java.nio.file.Path; // Added for FileUpload
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors; // Added for BasicResponseSynthesizer

// QwenClient.java content
/**
 * QwenSDK.java is a single-file, consolidated version of the Qwen SDK, designed for easy integration,
 * especially in environments like Sketchware Pro. It includes all necessary classes for interacting
 * with the Qwen (Tongyi Qianyuan) API.
 *
 * <h2>Usage Instructions:</h2>
 *
 * <h3>1. Initialization:</h3>
 * To use the SDK, first instantiate the {@code QwenClient}. You'll need your API key and cookie.
 * <pre>{@code
 * String apiKey = "YOUR_API_KEY"; // Replace with your actual API key
 * String cookie = "YOUR_COOKIE";   // Replace with your actual cookie
 * QwenClient client = new QwenClient(apiKey, cookie);
 *
 * // You can also specify a default model, base URL, timeout, etc.
 * // QwenClient client = new QwenClient(apiKey, cookie, "qwen-max", "https://chat.qwen.ai", 600, "INFO", false, "qwen-max");
 * }</pre>
 *
 * <h3>2. Making a Synchronous Chat Completion Request:</h3>
 * <pre>{@code
 * // 1. Instantiate the client (if not already done)
 * // QwenClient client = new QwenClient("YOUR_API_KEY", "YOUR_COOKIE");
 *
 * // 2. Create chat messages
 * List<ChatMessage> messages = new ArrayList<>();
 * messages.add(new ChatMessage(Role.USER, "Hello, what is AI?"));
 * // For multimodal messages with images, see File Upload and Multimodal Chat sections.
 *
 * // 3. Create a chat request
 * // Uses the default model specified during QwenClient initialization if not overridden here.
 * ChatRequest request = new ChatRequest(client.getDefaultModel(), messages);
 * // Optional: Set other parameters like temperature, maxTokens
 * // request.setTemperature(0.7f);
 * // request.setMaxTokens(1000);
 *
 * // 4. Make the call
 * try {
 *     ChatResponse response = client.getChatCompletion().create(request);
 *     if (response.getChoices() != null && !response.getChoices().isEmpty()) {
 *         System.out.println("Response: " + response.getChoices().get(0).getMessage().getContent());
 *     } else {
 *         System.out.println("No response choices received.");
 *     }
 * } catch (QwenApiError e) {
 *     System.err.println("API Error: " + e.getMessage() + " (Status: " + e.getStatusCode() + ")");
 *     e.printStackTrace();
 * }
 * }</pre>
 *
 * <h3>3. Synchronous File Upload (e.g., for Multimodal Chat):</h3>
 * <pre>{@code
 * // 1. Instantiate the client (if not already done)
 * // QwenClient client = new QwenClient("YOUR_API_KEY", "YOUR_COOKIE");
 *
 * // 2. Get the file to upload
 * File fileToUpload = new File("path/to/your/image.png"); // Ensure this file exists
 *
 * // 3. Make the call
 * if (fileToUpload.exists()) {
 *     try {
 *         FileUploadResponse fileResponse = client.getFileUpload().upload(fileToUpload);
 *         System.out.println("File uploaded successfully!");
 *         System.out.println("File ID: " + fileResponse.getFileId());
 *         System.out.println("File URL: " + fileResponse.getFileUrl());
 *         System.out.println("File Name: " + fileResponse.getFileName());
 *         // You can then use fileResponse.getFileUrl() or fileResponse.getFileId()
 *         // when constructing ImageBlocks for multimodal chat.
 *     } catch (QwenApiError e) {
 *         System.err.println("File Upload API Error: " + e.getMessage() + " (Status: " + e.getStatusCode() + ")");
 *         e.printStackTrace();
 *     }
 * } else {
 *     System.err.println("File not found: " + fileToUpload.getAbsolutePath());
 * }
 * }</pre>
 *
 * <h3>4. Multimodal Chat (Text and Image):</h3>
 * After uploading an image, use its URL to create an {@link ImageBlock}.
 * <pre>{@code
 * // Assume 'client' is initialized and 'fileResponse' is obtained from a successful file upload.
 * // String imageUrl = fileResponse.getFileUrl();
 * String imageUrl = "https://example.com/image.png"; // Replace with actual URL from upload
 * String imageMimeType = "image/png"; // Or get from fileResponse.getImageMimetype() if available
 *
 * List<Block> contentBlocks = new ArrayList<>();
 * contentBlocks.add(new TextBlock("Describe this image:"));
 * contentBlocks.add(new ImageBlock(imageUrl, imageMimeType));
 *
 * ChatMessage multimodalMessage = new ChatMessage(Role.USER, contentBlocks);
 *
 * List<ChatMessage> messages = new ArrayList<>();
 * messages.add(multimodalMessage);
 *
 * ChatRequest request = new ChatRequest(client.getDefaultModel(), messages);
 * // Ensure you are using a model that supports multimodal input, e.g., "qwen-vl-plus" or "qwen-vl-max"
 * // request.setModel("qwen-vl-plus");
 *
 * try {
 *     ChatResponse response = client.getChatCompletion().create(request);
 *     if (response.getChoices() != null && !response.getChoices().isEmpty()) {
 *         System.out.println("Multimodal Response: " + response.getChoices().get(0).getMessage().getContent());
 *     }
 * } catch (QwenApiError e) {
 *     e.printStackTrace();
 * }
 * }</pre>
 *
 * <h3>Asynchronous Operations:</h3>
 * Most operations like {@code createAsync} and {@code createStreamAsync} for chat,
 * and {@code uploadAsync} for files, are available. They require a {@link QwenCallback} implementation.
 *
 * <h3>Streaming Chat:</h3>
 * Use {@code client.getChatCompletion().createStream(request)} for an {@code Iterable<ChatResponseChunk>}
 * or {@code client.getChatCompletion().createStreamAsync(request, chunkCallback, completionCallback)} for fully async streaming.
 *
 * <h3>Dependency Note:</h3>
 * IMPORTANT: To use this SDK in your Sketchware Pro project, you MUST include the following libraries:
 * <ul>
 *     <li>OkHttp: {@code com.squareup.okhttp3:okhttp:4.10.0} (or a compatible version)</li>
 *     <li>Gson: {@code com.google.code.gson:gson:2.10.1} (or a compatible version)</li>
 * </ul>
 * Add these as dependencies in your Sketchware Pro project's library manager (e.g., in "Local Library" or "Gradle Dependencies").
 *
 * <h3>File Upload Note:</h3>
 * File Uploads: The SDK attempts to automatically determine the MIME type of files being uploaded using {@link java.nio.file.Files#probeContentType(Path)}
 * and falls back to common extensions. For common types like .png, .jpg, this usually works well. If you encounter issues with specific file types,
 * ensure they have standard file extensions or consider modifying the {@code determineMimeType} method if greater control is needed.
 *
 * @see AuthManager
 * @see ChatCompletion
 * @see FileUpload
 * @see ChatMessage
 * @see ChatRequest
 * @see ChatResponse
 * @see Block
 * @see ImageBlock
 * @see TextBlock
 * @see Role
 * @see QwenApiError
 * @see QwenCallback
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

// QwenCallback.java content
/**
 * A generic callback interface for handling asynchronous responses from the Qwen API.
 *
 * @param <T> The type of the successful response object.
 */
interface QwenCallback<T> { // `public` removed

    /**
     * Called when the API request is successful.
     *
     * @param response The response object of type T, parsed from the API's output.
     */
    void onSuccess(T response);

    /**
     * Called when the API request fails or an error occurs during processing.
     *
     * @param error A {@link QwenApiError} object containing details about the failure.
     */
    void onFailure(QwenApiError error);
}

// AuthManager.java content
/**
 * Manages authentication credentials for the Qwen API.
 * This class stores the API key and cookie required for making authenticated requests.
 */
class AuthManager { // `public` removed
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

// QwenApiError.java content
/**
 * Custom exception class for errors encountered while interacting with the Qwen API.
 * It can hold details like HTTP status codes and specific error messages from the API.
 */
class QwenApiError extends RuntimeException { // `public` removed
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

// Block.java content
/**
 * Represents a generic content block in a multimodal message.
 * Implementations of this interface define specific types of content, like text or images.
 */
interface Block { // `public` removed
    /**
     * Gets the type of this content block.
     *
     * @return The {@link BlockType} enum value indicating the nature of the block.
     */
    BlockType getBlockType();
}

// BlockType.java content
/**
 * Represents the type of content block within a multimodal chat message.
 */
enum BlockType { // `public` removed
    /**
     * A text content block.
     */
    @SerializedName("text")
    TEXT("text"),

    /**
     * An image content block.
     * Note: The Qwen API might use "image_url" for image references in some contexts.
     * This enum represents the conceptual block type. Serialization might adapt the name.
     */
    @SerializedName("image") // Or "image_url" depending on API specifics for request formatting
    IMAGE("image");

    private final String value;

    BlockType(String value) {
        this.value = value;
    }

    /**
     * Gets the string value of the block type.
     * @return The string value (e.g., "text", "image").
     */
    public String getValue() {
        return value;
    }

    /**
     * Creates a {@link BlockType} enum from its string representation.
     * This method is case-insensitive.
     *
     * @param text The string representation of the block type (e.g., "text", "image").
     * @return The corresponding {@link BlockType} enum.
     * @throws IllegalArgumentException if no matching block type is found for the given text.
     */
    public static BlockType fromString(String text) {
        if (text != null) {
            for (BlockType bt : BlockType.values()) {
                if (text.equalsIgnoreCase(bt.value)) {
                    return bt;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found in BlockType enum");
    }
}

// ChatMessage.java content
/**
 * Represents a single message in a chat conversation.
 * A message has a {@link Role}, content (which can be simple text or a list of {@link Block} objects for multimodal input),
 * and optional parameters like {@code webSearch}.
 * The actual "content" field sent to the API is constructed based on the {@code blocks} field during serialization.
 */
class ChatMessage { // `public` removed
    private Role role;

    /**
     * Primary field for structured/multimodal content.
     * If this message is simple text, this list will contain a single {@link TextBlock}.
     * During serialization, this list is transformed into the appropriate "content" JSON structure
     * (either a simple string or an array of typed content objects).
     */
    private List<Block> blocks;

    /**
     * Indicates whether web search should be enabled for processing this message.
     * Serialized as "web_search".
     */
    @SerializedName("web_search")
    private Boolean webSearch;

    /**
     * Indicates if the model is expected to "think" or use tools before responding.
     * This field might be more relevant in responses or specific API versions.
     */
    private Boolean thinking;

    /**
     * Budget for "thinking" steps, if applicable.
     * Serialized as "thinking_budget".
     */
    @SerializedName("thinking_budget")
    private Integer thinkingBudget;

    /**
     * Specifies the desired output schema for the response, if applicable (e.g., for structured output).
     * Serialized as "output_schema".
     */
    @SerializedName("output_schema")
    private String outputSchema;

    /**
     * Default constructor, primarily for use by Gson during deserialization.
     */
    public ChatMessage() {}

    /**
     * Constructs a new ChatMessage with a specific role and simple text content.
     *
     * @param role The {@link Role} of the message sender.
     * @param textContent The text content of the message. A {@link TextBlock} will be created for this.
     */
    public ChatMessage(Role role, String textContent) {
        this.role = role;
        if (textContent != null) {
            this.blocks = Collections.singletonList(new TextBlock(textContent));
        } else {
            this.blocks = new ArrayList<>(); // Or handle as error, depending on desired strictness
        }
    }

    /**
     * Constructs a new ChatMessage with a specific role and a list of content blocks for multimodal input.
     *
     * @param role The {@link Role} of the message sender.
     * @param blocks A list of {@link Block} objects representing the message content.
     */
    public ChatMessage(Role role, List<Block> blocks) {
        this.role = role;
        this.blocks = blocks;
    }

    /**
     * Gets the role of the message sender.
     * @return The {@link Role}.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the role of the message sender.
     * @param role The {@link Role}.
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Gets the list of content blocks for this message.
     * For simple text messages, this will typically contain a single {@link TextBlock}.
     * @return A list of {@link Block} objects.
     */
    public List<Block> getBlocks() {
        return blocks;
    }

    /**
     * Sets the list of content blocks for this message.
     * @param blocks A list of {@link Block} objects.
     */
    public void setBlocks(List<Block> blocks) {
        this.blocks = blocks;
    }

    /**
     * Helper method to retrieve the text content if this message consists of a single {@link TextBlock}.
     * This is primarily for convenience and is not directly used for JSON serialization of the "content" field,
     * as that logic is handled during the {@link ChatCompletion#buildRequestBody(ChatRequest)} process.
     *
     * @return The text content if it's a single TextBlock, otherwise null.
     */
    public String getSimpleTextContent() {
        if (blocks != null && blocks.size() == 1 && blocks.get(0) instanceof TextBlock) {
            return ((TextBlock) blocks.get(0)).getText();
        }
        return null;
    }

    /**
     * Checks if web search is enabled for this message.
     * @return True if web search is enabled, false otherwise. Can be null if not set.
     */
    public Boolean getWebSearch() {
        return webSearch;
    }

    /**
     * Enables or disables web search for processing this message.
     * @param webSearch True to enable, false to disable.
     */
    public void setWebSearch(Boolean webSearch) {
        this.webSearch = webSearch;
    }

    /**
     * Checks if "thinking" mode is enabled.
     * @return True if "thinking" is enabled, false otherwise. Can be null.
     */
    public Boolean getThinking() {
        return thinking;
    }

    /**
     * Sets the "thinking" mode.
     * @param thinking True to enable "thinking".
     */
    public void setThinking(Boolean thinking) {
        this.thinking = thinking;
    }

    /**
     * Gets the budget for "thinking" steps.
     * @return The thinking budget, or null if not set.
     */
    public Integer getThinkingBudget() {
        return thinkingBudget;
    }

    /**
     * Sets the budget for "thinking" steps.
     * @param thinkingBudget The budget value.
     */
    public void setThinkingBudget(Integer thinkingBudget) {
        this.thinkingBudget = thinkingBudget;
    }

    /**
     * Gets the desired output schema.
     * @return The output schema string, or null if not set.
     */
    public String getOutputSchema() {
        return outputSchema;
    }

    /**
     * Sets the desired output schema.
     * @param outputSchema The output schema string.
     */
    public void setOutputSchema(String outputSchema) {
        this.outputSchema = outputSchema;
    }
}

// ChatRequest.java content
/**
 * Represents a request payload for the Qwen Chat Completions API.
 * It includes the model to use, a list of messages forming the conversation history,
 * and various parameters controlling the generation process.
 */
class ChatRequest { // `public` removed
    private String model;
    private List<ChatMessage> messages;
    private Float temperature;

    /**
     * The maximum number of tokens to generate in the completion.
     * Serialized as "max_tokens".
     */
    @SerializedName("max_tokens")
    private Integer maxTokens;

    /**
     * Whether to stream the response incrementally.
     * This is typically set by the SDK method (e.g., {@code create} vs {@code createStream})
     * rather than directly by the user of this POJO.
     */
    private Boolean stream;

    /**
     * Controls whether to output content incrementally or after full generation,
     * especially relevant for streaming. Defaults to true.
     * Serialized as "incremental_output".
     */
    @SerializedName("incremental_output")
    private Boolean incrementalOutput;

    /**
     * Constructs a new ChatRequest with the specified model and messages.
     * Incremental output defaults to true.
     *
     * @param model The ID of the model to use for this request (e.g., "qwen-turbo").
     * @param messages A list of {@link ChatMessage} objects representing the conversation history.
     */
    public ChatRequest(String model, List<ChatMessage> messages) {
        this.model = model;
        this.messages = messages;
        this.incrementalOutput = true; // Default as per Qwen API common practice
    }

    /**
     * Default constructor, primarily for use by Gson during deserialization or for manual construction.
     * Incremental output defaults to true.
     */
    public ChatRequest() {
        this.incrementalOutput = true; // Default as per Qwen API common practice
    }

    /**
     * Gets the model ID.
     * @return The model ID.
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the model ID.
     * @param model The model ID (e.g., "qwen-turbo").
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Gets the list of messages.
     * @return A list of {@link ChatMessage} objects.
     */
    public List<ChatMessage> getMessages() {
        return messages;
    }

    /**
     * Sets the list of messages.
     * @param messages A list of {@link ChatMessage} objects.
     */
    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    /**
     * Gets the sampling temperature.
     * @return The temperature value, or null if not set.
     */
    public Float getTemperature() {
        return temperature;
    }

    /**
     * Sets the sampling temperature. Values typically range from 0.0 to 2.0.
     * Higher values make output more random, lower values make it more deterministic.
     * @param temperature The temperature value.
     */
    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    /**
     * Gets the maximum number of tokens to generate.
     * @return The maximum number of tokens, or null if not set.
     */
    public Integer getMaxTokens() {
        return maxTokens;
    }

    /**
     * Sets the maximum number of tokens to generate.
     * @param maxTokens The maximum number of tokens.
     */
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    /**
     * Checks if streaming is enabled for this request.
     * @return True if streaming is enabled, false otherwise. Can be null if not explicitly set.
     */
    public Boolean getStream() {
        return stream;
    }

    /**
     * Enables or disables streaming for this request.
     * This is usually handled by the specific SDK method called.
     * @param stream True to enable streaming.
     */
    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    /**
     * Checks if incremental output is enabled.
     * @return True if incremental output is enabled, false otherwise.
     */
    public Boolean getIncrementalOutput() {
        return incrementalOutput;
    }

    /**
     * Enables or disables incremental output.
     * @param incrementalOutput True to enable incremental output.
     */
    public void setIncrementalOutput(Boolean incrementalOutput) {
        this.incrementalOutput = incrementalOutput;
    }
}

// ChatResponse.java content
/**
 * Represents a non-streaming response from the Qwen Chat Completions API.
 * It includes a unique ID for the response, the model used, a list of choices (usually one),
 * and token usage statistics.
 */
class ChatResponse { // `public` removed
    private String id; // Unique identifier for the chat completion response
    private String model; // Model used for the completion
    private List<Choice> choices; // A list of chat completion choices. Usually n=1.
    private Usage usage; // Token usage statistics for the completion request

    /**
     * Default constructor, primarily for use by Gson during deserialization.
     */
    public ChatResponse() {}

    /**
     * Gets the unique identifier for this chat completion response.
     * @return The response ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this chat completion response.
     * @param id The response ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the ID of the model used to generate this response.
     * @return The model ID.
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the ID of the model used to generate this response.
     * @param model The model ID.
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Gets the list of chat completion choices.
     * Typically, this list contains a single choice unless multiple completions (n > 1) were requested.
     * @return A list of {@link Choice} objects.
     */
    public List<Choice> getChoices() {
        return choices;
    }

    /**
     * Sets the list of chat completion choices.
     * @param choices A list of {@link Choice} objects.
     */
    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    /**
     * Gets the token usage statistics for this completion request.
     * @return The {@link Usage} object containing token counts.
     */
    public Usage getUsage() {
        return usage;
    }

    /**
     * Sets the token usage statistics for this completion request.
     * @param usage The {@link Usage} object.
     */
    public void setUsage(Usage usage) {
        this.usage = usage;
    }
}

// ChatResponseChunk.java content
/**
 * Represents a chunk of data received in a streaming chat completion response.
 * This typically corresponds to a single Server-Sent Event (SSE) from the API.
 * The structure often mirrors {@link ChatResponse} but focuses on the {@code delta} within choices.
 */
class ChatResponseChunk { // `public` removed
    private String id; // Unique identifier for the chat completion or chunk
    private String model; // Model used for the completion

    /**
     * A list of choices, where each choice contains a {@code delta} field
     * representing the incremental update in a streaming response.
     */
    private List<Choice> choices;

    /**
     * Default constructor, primarily for use by Gson during deserialization.
     */
    public ChatResponseChunk() {}

    /**
     * Gets the unique identifier for this chat completion chunk.
     * @return The chunk ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this chat completion chunk.
     * @param id The chunk ID.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the ID of the model used to generate this chunk.
     * @return The model ID.
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the ID of the model used to generate this chunk.
     * @param model The model ID.
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * Gets the list of choices for this chunk.
     * In streaming, each {@link Choice} object in this list typically contains a {@code delta}
     * with the incremental content.
     * @return A list of {@link Choice} objects.
     */
    public List<Choice> getChoices() {
        return choices;
    }

    /**
     * Sets the list of choices for this chunk.
     * @param choices A list of {@link Choice} objects.
     */
    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
}

// Choice.java content
/**
 * Represents a choice in a chat completion response.
 * A response can have multiple choices, though typically there's one.
 * In non-streaming responses, {@code message} contains the full response message.
 * In streaming responses, {@code delta} contains the incremental update.
 */
class Choice { // `public` removed
    /**
     * The complete message object, typically used in non-streaming responses.
     */
    private ResponseMessage message;

    /**
     * The delta message object, used in streaming responses to provide incremental updates.
     */
    private ResponseMessage delta;

    /**
     * The reason the model stopped generating tokens.
     * Possible values include "stop", "length", "function_call", "content_filter", etc.
     * Serialized as "finish_reason".
     */
    @SerializedName("finish_reason")
    private String finishReason;

    /**
     * Default constructor, primarily for use by Gson during deserialization.
     */
    public Choice() {}

    /**
     * Gets the complete response message.
     * This is typically populated in non-streaming API responses.
     * @return The {@link ResponseMessage}, or null if not applicable (e.g., in a stream delta that's not the first one).
     */
    public ResponseMessage getMessage() {
        return message;
    }

    /**
     * Sets the complete response message.
     * @param message The {@link ResponseMessage}.
     */
    public void setMessage(ResponseMessage message) {
        this.message = message;
    }

    /**
     * Gets the delta (incremental update) for a streaming response.
     * This is populated in streaming API responses.
     * @return The delta {@link ResponseMessage}, or null if not applicable.
     */
    public ResponseMessage getDelta() {
        return delta;
    }

    /**
     * Sets the delta (incremental update) for a streaming response.
     * @param delta The delta {@link ResponseMessage}.
     */
    public void setDelta(ResponseMessage delta) {
        this.delta = delta;
    }

    /**
     * Gets the reason why the model finished generating tokens.
     * @return The finish reason string (e.g., "stop", "length").
     */
    public String getFinishReason() {
        return finishReason;
    }

    /**
     * Sets the reason why the model finished generating tokens.
     * @param finishReason The finish reason string.
     */
    public void setFinishReason(String finishReason) {
        this.finishReason = finishReason;
    }
}

// FileUploadResponse.java content
/**
 * Represents the response received from the Qwen API after a successful file upload.
 * It typically includes URLs, IDs, and metadata about the uploaded file.
 * This model is based on observations from the Python SDK's `FileData` object.
 */
class FileUploadResponse { // `public` removed

    /**
     * The URL where the uploaded file can be accessed, often a temporary or CDN URL.
     * Serialized as "file_url".
     */
    @SerializedName("file_url")
    private String fileUrl;

    /**
     * The MIME type of the uploaded image. This field might be specific to image uploads
     * and could be null or absent for other file types.
     * Serialized as "image_mimetype".
     */
    @SerializedName("image_mimetype")
    private String imageMimetype;

    /**
     * The unique identifier assigned to the uploaded file by the Qwen API.
     * Serialized as "file_id".
     */
    @SerializedName("file_id")
    private String fileId;

    /**
     * The name of the file as it was uploaded or as stored by the server.
     * Serialized as "file_name".
     */
    @SerializedName("file_name")
    private String fileName;

    /**
     * Default constructor, primarily for use by Gson during deserialization.
     */
    public FileUploadResponse() {}

    /**
     * Constructs a FileUploadResponse with all specified fields.
     *
     * @param fileUrl The URL of the uploaded file.
     * @param imageMimetype The MIME type of the image (can be null).
     * @param fileId The unique ID of the file.
     * @param fileName The name of the file.
     */
    public FileUploadResponse(String fileUrl, String imageMimetype, String fileId, String fileName) {
        this.fileUrl = fileUrl;
        this.imageMimetype = imageMimetype;
        this.fileId = fileId;
        this.fileName = fileName;
    }

    /**
     * Gets the URL of the uploaded file.
     * @return The file URL.
     */
    public String getFileUrl() {
        return fileUrl;
    }

    /**
     * Sets the URL of the uploaded file.
     * @param fileUrl The file URL.
     */
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    /**
     * Gets the MIME type of the uploaded image.
     * @return The image MIME type, or null if not applicable/available.
     */
    public String getImageMimetype() {
        return imageMimetype;
    }

    /**
     * Sets the MIME type of the uploaded image.
     * @param imageMimetype The image MIME type.
     */
    public void setImageMimetype(String imageMimetype) {
        this.imageMimetype = imageMimetype;
    }

    /**
     * Gets the unique ID of the uploaded file.
     * @return The file ID.
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * Sets the unique ID of the uploaded file.
     * @param fileId The file ID.
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /**
     * Gets the name of the uploaded file.
     * @return The file name.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the name of the uploaded file.
     * @param fileName The file name.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns a string representation of the FileUploadResponse object.
     * @return A string containing the values of the fields.
     */
    @Override
    public String toString() {
        return "FileUploadResponse{" +
               "fileUrl='" + fileUrl + '\'' +
               ", imageMimetype='" + imageMimetype + '\'' +
               ", fileId='" + fileId + '\'' +
               ", fileName='" + fileName + '\'' +
               '}';
    }
}

// FunctionCall.java content
/**
 * Represents a function call requested by the model as part of a chat response.
 * This is a placeholder structure; actual Qwen API might have more specific fields or formats.
 */
class FunctionCall { // `public` removed
    private String name;
    private String arguments; // Typically a JSON string representing the arguments

    /**
     * Default constructor, primarily for use by Gson during deserialization.
     */
    public FunctionCall() {}

    /**
     * Constructs a new FunctionCall with the specified name and arguments.
     *
     * @param name The name of the function to be called.
     * @param arguments A string, typically JSON formatted, representing the arguments for the function.
     */
    public FunctionCall(String name, String arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    /**
     * Gets the name of the function to be called.
     * @return The function name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the function to be called.
     * @param name The function name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the arguments for the function call.
     * These are often represented as a JSON string.
     * @return The function arguments string.
     */
    public String getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments for the function call.
     * @param arguments A string, typically JSON formatted, representing the arguments.
     */
    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
}

// ImageBlock.java content
/**
 * Represents an image content block within a multimodal message.
 * This class implements the {@link Block} interface and includes a URL
 * from which the image can be fetched and its MIME type.
 */
class ImageBlock implements Block { // `public` removed
    private String url;

    /**
     * The MIME type of the image (e.g., "image/png", "image/jpeg").
     * This field is serialized as "image_mimetype" in JSON.
     */
    @SerializedName("image_mimetype")
    private String imageMimetype;

    /**
     * Constructs a new ImageBlock with the specified image URL and MIME type.
     *
     * @param url The URL of the image. This could be a publicly accessible URL
     *            or a URL obtained from a file upload service (like Qwen's file upload).
     * @param imageMimetype The MIME type of the image (e.g., "image/png").
     */
    public ImageBlock(String url, String imageMimetype) {
        this.url = url;
        this.imageMimetype = imageMimetype;
    }

    /**
     * Gets the URL of the image.
     *
     * @return The image URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the image.
     *
     * @param url The new image URL.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the MIME type of the image.
     *
     * @return The image MIME type.
     */
    public String getImageMimetype() {
        return imageMimetype;
    }

    /**
     * Sets the MIME type of the image.
     *
     * @param imageMimetype The new image MIME type.
     */
    public void setImageMimetype(String imageMimetype) {
        this.imageMimetype = imageMimetype;
    }

    /**
     * {@inheritDoc}
     *
     * @return Always {@link BlockType#IMAGE}.
     */
    @Override
    public BlockType getBlockType() {
        return BlockType.IMAGE;
    }
}

// ResponseMessage.java content
/**
 * Represents a message received from the Qwen API in a chat response.
 * This can be a complete message or a delta in a streamed response.
 */
class ResponseMessage { // `public` removed
    private Role role;
    private String content; // Text content of the message. Can be null if function_call is present.

    /**
     * The function call requested by the model, if any.
     * Serialized as "function_call".
     */
    @SerializedName("function_call")
    private FunctionCall functionCall;

    /**
     * A map to hold any other non-standard fields that might be part of the response message.
     * This provides flexibility for API extensions.
     */
    private Map<String, Object> extra;

    /**
     * Default constructor, primarily for use by Gson during deserialization.
     */
    public ResponseMessage() {}

    /**
     * Constructs a ResponseMessage with a role and text content.
     * @param role The {@link Role} of the message sender (typically ASSISTANT).
     * @param content The text content of the message.
     */
    public ResponseMessage(Role role, String content) {
        this.role = role;
        this.content = content;
    }

    /**
     * Gets the role of the message sender.
     * @return The {@link Role}.
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the role of the message sender.
     * @param role The {@link Role}.
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Gets the text content of the message.
     * This may be null if the message represents a function call or other structured content.
     * @return The text content, or null.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the text content of the message.
     * @param content The text content.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the function call requested by the model, if any.
     * @return The {@link FunctionCall} object, or null if no function call was requested.
     */
    public FunctionCall getFunctionCall() {
        return functionCall;
    }

    /**
     * Sets the function call requested by the model.
     * @param functionCall The {@link FunctionCall} object.
     */
    public void setFunctionCall(FunctionCall functionCall) {
        this.functionCall = functionCall;
    }

    /**
     * Gets any extra, non-standard fields included in the message.
     * @return A map of extra fields, or null if none.
     */
    public Map<String, Object> getExtra() {
        return extra;
    }

    /**
     * Sets extra, non-standard fields for the message.
     * @param extra A map of extra fields.
     */
    public void setExtra(Map<String, Object> extra) {
        this.extra = extra;
    }
}

// Role.java content
/**
 * Represents the role of the message sender in a chat conversation.
 */
enum Role { // `public` removed
    /**
     * System message, often used to provide instructions or context to the model.
     */
    @SerializedName("system")
    SYSTEM("system"),

    /**
     * User message, representing input from the end-user.
     */
    @SerializedName("user")
    USER("user"),

    /**
     * Assistant message, representing a response from the AI model.
     */
    @SerializedName("assistant")
    ASSISTANT("assistant"),

    /**
     * Function message, representing the result of a function call requested by the model.
     * (Note: While defined, full function calling support might require additional specific request/response fields.)
     */
    @SerializedName("function")
    FUNCTION("function");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    /**
     * Gets the string value of the role, as expected by the API.
     * @return The string value of the role (e.g., "system", "user").
     */
    public String getValue() {
        return value;
    }

    /**
     * Creates a {@link Role} enum from its string representation.
     * This method is case-insensitive.
     *
     * @param text The string representation of the role (e.g., "system", "user").
     * @return The corresponding {@link Role} enum.
     * @throws IllegalArgumentException if no matching role is found for the given text.
     */
    public static Role fromString(String text) {
        if (text != null) {
            for (Role r : Role.values()) {
                if (text.equalsIgnoreCase(r.value)) {
                    return r;
                }
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found in Role enum");
    }
}

// TextBlock.java content
/**
 * Represents a text content block within a multimodal message.
 * This class implements the {@link Block} interface.
 */
class TextBlock implements Block { // `public` removed
    private String text;

    /**
     * Constructs a new TextBlock with the specified text content.
     *
     * @param text The text content for this block.
     */
    public TextBlock(String text) {
        this.text = text;
    }

    /**
     * Gets the text content of this block.
     *
     * @return The text content.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text content of this block.
     *
     * @param text The new text content.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * {@inheritDoc}
     *
     * @return Always {@link BlockType#TEXT}.
     */
    @Override
    public BlockType getBlockType() {
        return BlockType.TEXT;
    }
}

// Usage.java content
/**
 * Represents token usage statistics for a chat completion request.
 * This includes the number of tokens in the prompt, the completion, and the total.
 */
class Usage { // `public` removed
    /**
     * The number of tokens in the input prompt.
     * Serialized as "prompt_tokens".
     */
    @SerializedName("prompt_tokens")
    private int promptTokens;

    /**
     * The number of tokens in the generated completion.
     * Also referred to as "output_tokens" or "generated_tokens" in some contexts.
     * Serialized as "completion_tokens".
     */
    @SerializedName("completion_tokens")
    private int completionTokens;

    /**
     * The total number of tokens used in the request (prompt + completion).
     * Serialized as "total_tokens".
     */
    @SerializedName("total_tokens")
    private int totalTokens;

    /**
     * Default constructor, primarily for use by Gson during deserialization.
     */
    public Usage() {}

    /**
     * Gets the number of tokens in the prompt.
     * @return The count of prompt tokens.
     */
    public int getPromptTokens() {
        return promptTokens;
    }

    /**
     * Sets the number of tokens in the prompt.
     * @param promptTokens The count of prompt tokens.
     */
    public void setPromptTokens(int promptTokens) {
        this.promptTokens = promptTokens;
    }

    /**
     * Gets the number of tokens in the generated completion.
     * @return The count of completion tokens.
     */
    public int getCompletionTokens() {
        return completionTokens;
    }

    /**
     * Sets the number of tokens in the generated completion.
     * @param completionTokens The count of completion tokens.
     */
    public void setCompletionTokens(int completionTokens) {
        this.completionTokens = completionTokens;
    }

    /**
     * Gets the total number of tokens used.
     * @return The total token count.
     */
    public int getTotalTokens() {
        return totalTokens;
    }

    /**
     * Sets the total number of tokens used.
     * @param totalTokens The total token count.
     */
    public void setTotalTokens(int totalTokens) {
        this.totalTokens = totalTokens;
    }
}

// BasicResponseSynthesizer.java content
class BasicResponseSynthesizer implements ResponseSynthesizer { // `public` removed

    private final QwenClient qwenClient;
    private final String promptTemplate;
    private final ChatCompletion chatCompletion; // Store reference

    public static final String DEFAULT_PROMPT_TEMPLATE =
            "Context information is below.\n" +
            "---------------------\n" +
            "{context_str}\n" +
            "---------------------\n" +
            "Given the context information and not prior knowledge, answer the query.\n" +
            "Query: {query_str}\n" +
            "Answer:";

    public BasicResponseSynthesizer(QwenClient qwenClient) {
        this(qwenClient, DEFAULT_PROMPT_TEMPLATE);
    }

    public BasicResponseSynthesizer(QwenClient qwenClient, String promptTemplate) {
        if (qwenClient == null) {
            throw new IllegalArgumentException("QwenClient cannot be null.");
        }
        if (promptTemplate == null || promptTemplate.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt template cannot be null or empty.");
        }
        this.qwenClient = qwenClient;
        this.promptTemplate = promptTemplate;
        this.chatCompletion = qwenClient.getChatCompletion(); // Get ChatCompletion instance
         if (this.chatCompletion == null) {
            throw new IllegalStateException("ChatCompletion resource not available from QwenClient.");
        }
    }

    @Override
    public ChatResponse synthesize(String query, List<Document> documents) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty.");
        }
        if (documents == null) { // Empty list is acceptable, null is not.
            throw new IllegalArgumentException("Documents list cannot be null.");
        }

        String contextStr = documents.stream()
                                   .map(doc -> doc.getContent() != null ? doc.getContent() : "")
                                   .collect(Collectors.joining("\n\n"));

        String formattedPrompt = promptTemplate
                                   .replace("{context_str}", contextStr)
                                   .replace("{query_str}", query);

        ChatMessage userMessage = new ChatMessage(Role.USER, formattedPrompt);

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessages(Collections.singletonList(userMessage));

        String model = qwenClient.getDefaultModel();
        if (model == null || model.trim().isEmpty()){
            throw new IllegalStateException("Default model is not configured in QwenClient or is invalid.");
        }
        chatRequest.setModel(model);

        try {
            return chatCompletion.create(chatRequest);
        } catch (QwenApiError e) {
            throw e;
        }
    }

    @Override
    public void synthesizeAsync(String query, List<Document> documents, QwenCallback<ChatResponse> callback) {
        if (query == null || query.trim().isEmpty()) {
            if (callback != null) {
                callback.onFailure(new QwenApiError("Query cannot be null or empty.", new IllegalArgumentException("Query is null or empty.")));
            }
            return;
        }
        if (documents == null) {
             if (callback != null) {
                callback.onFailure(new QwenApiError("Documents list cannot be null.", new IllegalArgumentException("Documents list is null.")));
            }
            return;
        }
         if (callback == null) {
            qwenClient.getLogger().warning("SynthesizeAsync called with null callback.");
            return;
        }

        String contextStr = documents.stream()
                                   .map(doc -> doc.getContent() != null ? doc.getContent() : "")
                                   .collect(Collectors.joining("\n\n"));

        String formattedPrompt = promptTemplate
                                   .replace("{context_str}", contextStr)
                                   .replace("{query_str}", query);

        ChatMessage userMessage = new ChatMessage(Role.USER, formattedPrompt);

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessages(Collections.singletonList(userMessage));

        String model = qwenClient.getDefaultModel();
        if (model == null || model.trim().isEmpty()){
            callback.onFailure(new QwenApiError("Default model is not configured or is invalid.", new IllegalStateException("Default model not configured.")));
            return;
        }
        chatRequest.setModel(model);

        chatCompletion.createAsync(chatRequest, callback);
    }
}

// Document.java content
interface Document { // `public` removed
    String getId();
    String getContent();
    Map<String, Object> getMetadata();
}

// QueryEngine.java content
class QueryEngine { // `public` removed
    private final Retriever retriever;
    private final ResponseSynthesizer responseSynthesizer;

    public QueryEngine(Retriever retriever, ResponseSynthesizer responseSynthesizer) {
        if (retriever == null) {
            throw new IllegalArgumentException("Retriever cannot be null.");
        }
        if (responseSynthesizer == null) {
            throw new IllegalArgumentException("ResponseSynthesizer cannot be null.");
        }
        this.retriever = retriever;
        this.responseSynthesizer = responseSynthesizer;
    }

    public ChatResponse query(String userQuery) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("User query cannot be null or empty.");
        }
        List<Document> documents = retriever.retrieve(userQuery);
        return responseSynthesizer.synthesize(userQuery, documents);
    }

    public void queryAsync(String userQuery, final QwenCallback<ChatResponse> callback) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            if (callback != null) {
                callback.onFailure(new QwenApiError("User query cannot be null or empty.",
                                   new IllegalArgumentException("User query cannot be null or empty.")));
            }
            return;
        }
        if (callback == null) {
            return;
        }

        retriever.retrieveAsync(userQuery, new QwenCallback<List<Document>>() {
            @Override
            public void onSuccess(List<Document> documents) {
                responseSynthesizer.synthesizeAsync(userQuery, documents, callback);
            }

            @Override
            public void onFailure(QwenApiError error) {
                callback.onFailure(error);
            }
        });
    }
}

// ResponseSynthesizer.java content
interface ResponseSynthesizer { // `public` removed
    ChatResponse synthesize(String query, List<Document> documents);
    void synthesizeAsync(String query, List<Document> documents, QwenCallback<ChatResponse> callback);
}

// Retriever.java content
interface Retriever { // `public` removed
    List<Document> retrieve(String query);
    void retrieveAsync(String query, QwenCallback<List<Document>> callback);
}

// ChatCompletion.java content
/**
 * Handles Chat Completion operations for the Qwen API.
 * This class provides methods for creating synchronous, asynchronous, and streaming chat completions.
 */
class ChatCompletion { // `public` removed
    private final QwenClient qwenClient;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String completionsUrl;

    /**
     * Constructs a new ChatCompletion resource handler.
     *
     * @param qwenClient The {@link QwenClient} instance to use for API calls and configuration.
     * @throws IllegalArgumentException if qwenClient is null.
     */
    public ChatCompletion(QwenClient qwenClient) {
        if (qwenClient == null) {
            throw new IllegalArgumentException("QwenClient cannot be null.");
        }
        this.qwenClient = qwenClient;
        this.httpClient = qwenClient.getHttpClient();
        this.gson = qwenClient.getGson();
        this.completionsUrl = qwenClient.getBaseUrl() + "/v1/chat/completions";
    }

    /**
     * Builds the JSON request body for a chat completion request.
     * This method handles the custom serialization of {@link ChatMessage#blocks} into
     * the API-expected "content" field (string or array of typed blocks).
     *
     * @param chatRequest The {@link ChatRequest} object.
     * @return The {@link RequestBody} to be sent.
     */
    private RequestBody buildRequestBody(ChatRequest chatRequest) {
        JsonObject requestJson = gson.toJsonTree(chatRequest).getAsJsonObject();
        JsonArray messagesJson = requestJson.getAsJsonArray("messages");
        JsonArray newMessagesJson = new JsonArray();

        for (int i = 0; i < messagesJson.size(); i++) {
            JsonObject messageJson = messagesJson.get(i).getAsJsonObject();
            ChatMessage originalMessage = chatRequest.getMessages().get(i);

            if (originalMessage.getBlocks() != null && !originalMessage.getBlocks().isEmpty()) {
                if (originalMessage.getBlocks().size() == 1 && originalMessage.getBlocks().get(0) instanceof TextBlock) {
                    TextBlock textBlock = (TextBlock) originalMessage.getBlocks().get(0);
                    messageJson.add("content", new JsonPrimitive(textBlock.getText()));
                } else {
                    JsonArray contentBlocks = new JsonArray();
                    for (Block block : originalMessage.getBlocks()) {
                        JsonObject contentBlock = new JsonObject();
                        if (block instanceof TextBlock) {
                            contentBlock.addProperty("type", "text");
                            contentBlock.addProperty("text", ((TextBlock) block).getText());
                        } else if (block instanceof ImageBlock) {
                            contentBlock.addProperty("type", "image_url");
                            JsonObject imageUrlObject = new JsonObject();
                            imageUrlObject.addProperty("url", ((ImageBlock) block).getUrl());
                            contentBlock.add("image_url", imageUrlObject);
                        }
                        contentBlocks.add(contentBlock);
                    }
                    messageJson.add("content", contentBlocks);
                }
            }
            messageJson.remove("blocks");
            newMessagesJson.add(messageJson);
        }
        requestJson.add("messages", newMessagesJson);

        String json = gson.toJson(requestJson);
        qwenClient.getLogger().fine("Request JSON: " + json);
        return RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
    }

    /**
     * Creates a non-streaming chat completion.
     *
     * @param chatRequest The {@link ChatRequest} detailing the model, messages, and other parameters.
     * @return A {@link ChatResponse} containing the model's reply.
     * @throws QwenApiError if the API request fails or an error occurs during processing.
     * @throws IllegalArgumentException if chatRequest is null.
     */
    public ChatResponse create(ChatRequest chatRequest) throws QwenApiError {
        if (chatRequest == null) {
            throw new IllegalArgumentException("ChatRequest cannot be null.");
        }
        chatRequest.setStream(false);
        RequestBody body = buildRequestBody(chatRequest);
        Request request = new Request.Builder()
                .url(completionsUrl)
                .post(body)
                .addHeader("Authorization", qwenClient.getAuthManager().getAuthorizationHeader())
                .addHeader("Cookie", qwenClient.getAuthManager().getCookieHeader())
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBodyStr = response.body() != null ? response.body().string() : "Unknown API error";
                qwenClient.getLogger().severe("API Error: " + response.code() + " " + errorBodyStr);
                throw new QwenApiError("API request failed with status code " + response.code() + ": " + errorBodyStr, response.code(), errorBodyStr);
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new QwenApiError("Empty response body from API", response.code(), "Empty response body");
            }
            String responseJson = responseBody.string();
            qwenClient.getLogger().fine("Response JSON: " + responseJson);
            return gson.fromJson(responseJson, ChatResponse.class);
        } catch (IOException e) {
            qwenClient.getLogger().log(Level.SEVERE, "IOException during synchronous chat completion", e);
            throw new QwenApiError("IOException during API request: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a non-streaming chat completion asynchronously.
     *
     * @param chatRequest The {@link ChatRequest} detailing the model, messages, and other parameters.
     * @param callback The {@link QwenCallback} to handle the response or failure.
     * @throws IllegalArgumentException if chatRequest or callback is null.
     */
    public void createAsync(ChatRequest chatRequest, final QwenCallback<ChatResponse> callback) {
        if (chatRequest == null) {
            throw new IllegalArgumentException("ChatRequest cannot be null.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null for asynchronous operations.");
        }
        chatRequest.setStream(false);
        RequestBody body = buildRequestBody(chatRequest);
        Request request = new Request.Builder()
                .url(completionsUrl)
                .post(body)
                .addHeader("Authorization", qwenClient.getAuthManager().getAuthorizationHeader())
                .addHeader("Cookie", qwenClient.getAuthManager().getCookieHeader())
                .addHeader("Accept", "application/json")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                qwenClient.getLogger().log(Level.SEVERE, "IOException during asynchronous chat completion", e);
                callback.onFailure(new QwenApiError("IOException during API request: " + e.getMessage(), e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        String errorBodyStr = responseBody != null ? responseBody.string() : "Unknown API error";
                        qwenClient.getLogger().severe("API Error: " + response.code() + " " + errorBodyStr);
                        callback.onFailure(new QwenApiError("API request failed with status code " + response.code() + ": " + errorBodyStr, response.code(), errorBodyStr));
                        return;
                    }
                    if (responseBody == null) {
                        callback.onFailure(new QwenApiError("Empty response body from API", response.code(), "Empty response body"));
                        return;
                    }
                    String responseJson = responseBody.string();
                    qwenClient.getLogger().fine("Response JSON: " + responseJson);
                    ChatResponse chatResponse = gson.fromJson(responseJson, ChatResponse.class);
                    callback.onSuccess(chatResponse);
                } catch (Exception e) {
                    qwenClient.getLogger().log(Level.SEVERE, "Exception processing asynchronous response", e);
                    callback.onFailure(new QwenApiError("Failed to process API response: " + e.getMessage(), e));
                }
            }
        });
    }

    /**
     * Creates a streaming chat completion, returning an {@link Iterable} of {@link ChatResponseChunk}.
     * This method makes a blocking HTTP call and then provides an iterator for consuming SSE events.
     *
     * @param chatRequest The {@link ChatRequest} set up for streaming.
     * @return An {@link Iterable} that yields {@link ChatResponseChunk} objects as they are received.
     * @throws QwenApiError if the initial API request fails.
     * @throws IllegalArgumentException if chatRequest is null.
     */
    public Iterable<ChatResponseChunk> createStream(ChatRequest chatRequest) throws QwenApiError {
        if (chatRequest == null) {
            throw new IllegalArgumentException("ChatRequest cannot be null.");
        }
        chatRequest.setStream(true);
        RequestBody body = buildRequestBody(chatRequest);
        Request request = new Request.Builder()
                .url(completionsUrl)
                .post(body)
                .addHeader("Authorization", qwenClient.getAuthManager().getAuthorizationHeader())
                .addHeader("Cookie", qwenClient.getAuthManager().getCookieHeader())
                .addHeader("Accept", "text/event-stream")
                .build();

        try {
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                String errorBodyStr = response.body() != null ? response.body().string() : "Unknown API error";
                response.close();
                qwenClient.getLogger().severe("API Error for stream: " + response.code() + " " + errorBodyStr);
                throw new QwenApiError("API request failed for stream with status code " + response.code() + ": " + errorBodyStr, response.code(), errorBodyStr);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                response.close();
                throw new QwenApiError("Empty response body from API for stream", response.code(), "Empty response body");
            }
            return () -> new SseIterator(responseBody.charStream(), gson, qwenClient.getLogger(), response);

        } catch (IOException e) {
            qwenClient.getLogger().log(Level.SEVERE, "IOException during synchronous stream chat completion", e);
            throw new QwenApiError("IOException during API stream request: " + e.getMessage(), e);
        }
    }

    /**
     * Iterator for processing Server-Sent Events (SSE) from a character stream.
     * Parses "data:" lines into {@link ChatResponseChunk} objects.
     */
    private static class SseIterator implements Iterator<ChatResponseChunk> {
        private final BufferedReader reader;
        private final Gson gson;
        private final Logger logger;
        private ChatResponseChunk nextChunk;
        private boolean finished = false;
        private final Response responseToClose;

        public SseIterator(java.io.Reader streamReader, Gson gson, Logger logger, Response responseToClose) {
            this.reader = new BufferedReader(streamReader);
            this.gson = gson;
            this.logger = logger;
            this.nextChunk = null;
            this.responseToClose = responseToClose;
        }

        @Override
        public boolean hasNext() {
            if (finished) return false;
            if (nextChunk != null) return true;

            try {
                String line;
                StringBuilder dataBuffer = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    logger.finest("SSE Line: " + line);
                    if (line.startsWith("data:")) {
                        dataBuffer.append(line.substring(5).trim());
                    } else if (line.isEmpty() && dataBuffer.length() > 0) {
                        String jsonData = dataBuffer.toString();
                        logger.fine("SSE Data JSON: " + jsonData);
                        if ("[DONE]".equalsIgnoreCase(jsonData.trim())) {
                            finished = true;
                            closeResources();
                            return false;
                        }
                        try {
                            nextChunk = gson.fromJson(jsonData, ChatResponseChunk.class);
                            if (nextChunk != null) {
                                return true;
                            }
                        } catch (com.google.gson.JsonSyntaxException e) {
                            logger.log(Level.WARNING, "Failed to parse SSE JSON: " + jsonData, e);
                        }
                        dataBuffer.setLength(0);
                    }
                }
                finished = true;
                closeResources();
                return false;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "IOException reading SSE stream", e);
                finished = true;
                closeResources();
                return false;
            }
        }

        @Override
        public ChatResponseChunk next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more chunks in SSE stream.");
            }
            ChatResponseChunk chunkToReturn = nextChunk;
            nextChunk = null;
            return chunkToReturn;
        }

        private void closeResources() {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing SSE stream reader", e);
            }
            if (responseToClose != null) {
                responseToClose.close();
            }
        }
    }

    /**
     * Creates a streaming chat completion asynchronously using OkHttp's EventSource.
     *
     * @param chatRequest The {@link ChatRequest} set up for streaming.
     * @param chunkCallback The {@link QwenCallback} to handle each incoming {@link ChatResponseChunk}.
     * @param completionCallback The {@link QwenCallback} to signal the completion or failure of the entire stream.
     * @throws IllegalArgumentException if chatRequest, chunkCallback, or completionCallback is null.
     */
    public void createStreamAsync(ChatRequest chatRequest,
                                  final QwenCallback<ChatResponseChunk> chunkCallback,
                                  final QwenCallback<Void> completionCallback) {
        if (chatRequest == null) {
            throw new IllegalArgumentException("ChatRequest cannot be null.");
        }
        if (chunkCallback == null) {
            throw new IllegalArgumentException("ChunkCallback cannot be null for asynchronous streaming.");
        }
         if (completionCallback == null) {
            throw new IllegalArgumentException("CompletionCallback cannot be null for asynchronous streaming.");
        }

        chatRequest.setStream(true);
        RequestBody body = buildRequestBody(chatRequest);
        Request request = new Request.Builder()
                .url(completionsUrl)
                .post(body)
                .addHeader("Authorization", qwenClient.getAuthManager().getAuthorizationHeader())
                .addHeader("Cookie", qwenClient.getAuthManager().getCookieHeader())
                .addHeader("Accept", "text/event-stream")
                .addHeader("Cache-Control", "no-cache")
                .build();

        EventSourceListener listener = new EventSourceListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                qwenClient.getLogger().info("SSE connection opened.");
            }

            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                qwenClient.getLogger().fine("SSE Event: id=" + id + ", type=" + type + ", data=" + data);
                if ("[DONE]".equalsIgnoreCase(data.trim())) {
                    return;
                }
                try {
                    ChatResponseChunk chunk = gson.fromJson(data, ChatResponseChunk.class);
                    chunkCallback.onSuccess(chunk);
                } catch (com.google.gson.JsonSyntaxException e) {
                    qwenClient.getLogger().log(Level.WARNING, "Failed to parse SSE JSON data: " + data, e);
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                qwenClient.getLogger().info("SSE connection closed by server.");
                completionCallback.onSuccess(null);
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                String errorMessage = "SSE connection failed";
                int statusCode = 0;
                String errorBodyDetails = "";

                if (t != null) {
                    errorMessage += ": " + t.getMessage();
                    qwenClient.getLogger().log(Level.SEVERE, "SSE failure", t);
                }
                if (response != null) {
                    statusCode = response.code();
                    errorMessage += " (status code: " + statusCode + ")";
                    try (ResponseBody responseBody = response.body()){
                        if (responseBody != null) {
                           errorBodyDetails = responseBody.string();
                           qwenClient.getLogger().warning(errorMessage + (errorBodyDetails.isEmpty() ? "" : " - Check details if available."));
                        }
                    } catch (IOException e) {
                         qwenClient.getLogger().log(Level.WARNING, "Error reading error response body in SSE failure", e);
                    } finally {
                        if(response != null) response.close();
                    }
                } else if (t instanceof IOException) {
                     qwenClient.getLogger().log(Level.SEVERE, "SSE network failure", t);
                }

                QwenApiError apiError = new QwenApiError(errorMessage + (t != null ? " Cause: " + t.toString() : ""), statusCode, errorBodyDetails);
                if(t != null && !(t instanceof IOException)) apiError = new QwenApiError(errorMessage, t);

                completionCallback.onFailure(apiError);
            }
        };

        EventSources.createFactory(this.httpClient).newEventSource(request, listener);
        qwenClient.getLogger().info("Async SSE request initiated.");
    }
}

// FileUpload.java content
/**
 * Handles File Upload operations for the Qwen API.
 * This class provides methods for uploading files (e.g., images for multimodal chat)
 * both synchronously and asynchronously.
 */
class FileUpload { // `public` removed
    private final QwenClient qwenClient;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String fileUploadUrl;

    /**
     * Constructs a new FileUpload resource handler.
     *
     * @param qwenClient The {@link QwenClient} instance to use for API calls and configuration.
     * @throws IllegalArgumentException if qwenClient is null.
     */
    public FileUpload(QwenClient qwenClient) {
        if (qwenClient == null) {
            throw new IllegalArgumentException("QwenClient cannot be null.");
        }
        this.qwenClient = qwenClient;
        this.httpClient = qwenClient.getHttpClient();
        this.gson = qwenClient.getGson();
        this.fileUploadUrl = qwenClient.getBaseUrl() + "/api/upload_file";
    }

    /**
     * Determines the MIME type of a file.
     * It first attempts to use {@link java.nio.file.Files#probeContentType(Path)}.
     * If that fails or returns null, it falls back to a simple extension-based lookup.
     *
     * @param fileName The name of the file, used for extension-based fallback.
     * @param file The {@link File} object itself, used for {@code probeContentType}.
     * @return The determined MIME type string, or "application/octet-stream" as a default.
     */
    private String determineMimeType(String fileName, File file) {
        if (file != null && file.exists()) {
            try {
                Path path = file.toPath();
                String mimeType = Files.probeContentType(path);
                if (mimeType != null) {
                    return mimeType;
                }
            } catch (IOException e) {
                qwenClient.getLogger().log(Level.WARNING, "Failed to probe MIME type for " + fileName + " using java.nio.file.Files.probeContentType. Falling back to extension.", e);
            } catch (SecurityException e) {
                 qwenClient.getLogger().log(Level.WARNING, "SecurityException while probing MIME type for " + fileName + ". Falling back to extension.", e);
            }
        }

        if (fileName != null) {
            String lowerName = fileName.toLowerCase();
            if (lowerName.endsWith(".png")) return "image/png";
            if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) return "image/jpeg";
            if (lowerName.endsWith(".gif")) return "image/gif";
            if (lowerName.endsWith(".webp")) return "image/webp";
            if (lowerName.endsWith(".txt")) return "text/plain";
        }
        qwenClient.getLogger().info("Could not determine MIME type for " + fileName + " more accurately. Defaulting to application/octet-stream.");
        return "application/octet-stream";
    }

    /**
     * Uploads a file to the Qwen API synchronously.
     *
     * @param file The {@link File} to be uploaded. Must not be null and must exist.
     * @return A {@link FileUploadResponse} containing details about the uploaded file from the API.
     * @throws QwenApiError if the file does not exist, the API request fails, or an error occurs during processing.
     * @throws IllegalArgumentException if file is null.
     */
    public FileUploadResponse upload(File file) throws QwenApiError {
        if (file == null) {
            throw new IllegalArgumentException("File to upload cannot be null.");
        }
        if (!file.exists()) {
            throw new QwenApiError("File does not exist: " + file.getAbsolutePath(), new java.io.FileNotFoundException(file.getAbsolutePath()));
        }

        String mimeType = determineMimeType(file.getName(), file);
        qwenClient.getLogger().info("Uploading file: " + file.getName() + " with determined MIME type: " + mimeType);

        RequestBody fileBody = RequestBody.create(file, MediaType.parse(mimeType));
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(fileUploadUrl)
                .post(requestBody)
                .addHeader("Authorization", qwenClient.getAuthManager().getAuthorizationHeader())
                .addHeader("Cookie", qwenClient.getAuthManager().getCookieHeader())
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBodyStr = response.body() != null ? response.body().string() : "Unknown API error during file upload.";
                qwenClient.getLogger().severe("File Upload API Error: " + response.code() + " " + errorBodyStr);
                throw new QwenApiError("File upload failed with status code " + response.code(), response.code(), errorBodyStr);
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new QwenApiError("Empty response body from file upload API", response.code(), "Empty response body");
            }
            String responseJson = responseBody.string();
            qwenClient.getLogger().fine("File Upload Response JSON: " + responseJson);
            return gson.fromJson(responseJson, FileUploadResponse.class);
        } catch (IOException e) {
            qwenClient.getLogger().log(Level.SEVERE, "IOException during synchronous file upload", e);
            throw new QwenApiError("IOException during file upload API request: " + e.getMessage(), e);
        }
    }

    /**
     * Uploads a file to the Qwen API asynchronously.
     *
     * @param file The {@link File} to be uploaded.
     * @param callback The {@link QwenCallback} to handle the {@link FileUploadResponse} or failure.
     * @throws IllegalArgumentException if file or callback is null.
     */
    public void uploadAsync(final File file, final QwenCallback<FileUploadResponse> callback) {
        if (file == null) {
            throw new IllegalArgumentException("File to upload cannot be null.");
        }
         if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null for asynchronous operations.");
        }
        if (!file.exists()) {
            callback.onFailure(new QwenApiError("File does not exist: " + file.getAbsolutePath(), new java.io.FileNotFoundException(file.getAbsolutePath())));
            return;
        }

        String mimeType = determineMimeType(file.getName(), file);
        qwenClient.getLogger().info("Async uploading file: " + file.getName() + " with determined MIME type: " + mimeType);

        RequestBody fileBody = RequestBody.create(file, MediaType.parse(mimeType));
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(fileUploadUrl)
                .post(requestBody)
                .addHeader("Authorization", qwenClient.getAuthManager().getAuthorizationHeader())
                .addHeader("Cookie", qwenClient.getAuthManager().getCookieHeader())
                .addHeader("Accept", "application/json")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                qwenClient.getLogger().log(Level.SEVERE, "IOException during asynchronous file upload", e);
                callback.onFailure(new QwenApiError("IOException during file upload API request: " + e.getMessage(), e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        String errorBodyStr = responseBody != null ? responseBody.string() : "Unknown API error during file upload.";
                        qwenClient.getLogger().severe("File Upload API Error: " + response.code() + " " + errorBodyStr);
                        callback.onFailure(new QwenApiError("File upload failed with status code " + response.code(), response.code(), errorBodyStr));
                        return;
                    }
                    if (responseBody == null) {
                        callback.onFailure(new QwenApiError("Empty response body from file upload API", response.code(), "Empty response body"));
                        return;
                    }
                    String responseJson = responseBody.string();
                    qwenClient.getLogger().fine("File Upload Response JSON: " + responseJson);
                    FileUploadResponse fileUploadResponse = gson.fromJson(responseJson, FileUploadResponse.class);
                    callback.onSuccess(fileUploadResponse);
                } catch (Exception e) {
                    qwenClient.getLogger().log(Level.SEVERE, "Exception processing asynchronous file upload response", e);
                    callback.onFailure(new QwenApiError("Failed to process file upload API response: " + e.getMessage(), e));
                }
            }
        });
    }
}
