package com.example.qwen_sdk.models;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a choice in a chat completion response.
 * A response can have multiple choices, though typically there's one.
 * In non-streaming responses, {@code message} contains the full response message.
 * In streaming responses, {@code delta} contains the incremental update.
 */
public class Choice {
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
