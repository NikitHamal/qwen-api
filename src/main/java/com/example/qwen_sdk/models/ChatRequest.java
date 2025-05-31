package com.example.qwen_sdk.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Represents a request payload for the Qwen Chat Completions API.
 * It includes the model to use, a list of messages forming the conversation history,
 * and various parameters controlling the generation process.
 */
public class ChatRequest {
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
