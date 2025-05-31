package com.example.qwen_sdk.models;

import java.util.List;
// import com.google.gson.annotations.SerializedName; // If field names differ

/**
 * Represents a non-streaming response from the Qwen Chat Completions API.
 * It includes a unique ID for the response, the model used, a list of choices (usually one),
 * and token usage statistics.
 */
public class ChatResponse {
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
