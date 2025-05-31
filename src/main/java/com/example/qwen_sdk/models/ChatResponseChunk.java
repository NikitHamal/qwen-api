package com.example.qwen_sdk.models;

import java.util.List;
// import com.google.gson.annotations.SerializedName; // If field names differ

/**
 * Represents a chunk of data received in a streaming chat completion response.
 * This typically corresponds to a single Server-Sent Event (SSE) from the API.
 * The structure often mirrors {@link ChatResponse} but focuses on the {@code delta} within choices.
 */
public class ChatResponseChunk {
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
