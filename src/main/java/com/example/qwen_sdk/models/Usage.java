package com.example.qwen_sdk.models;

import com.google.gson.annotations.SerializedName;

/**
 * Represents token usage statistics for a chat completion request.
 * This includes the number of tokens in the prompt, the completion, and the total.
 */
public class Usage {
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
