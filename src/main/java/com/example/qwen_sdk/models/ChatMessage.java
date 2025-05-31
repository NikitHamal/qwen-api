package com.example.qwen_sdk.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents a single message in a chat conversation.
 * A message has a {@link Role}, content (which can be simple text or a list of {@link Block} objects for multimodal input),
 * and optional parameters like {@code webSearch}.
 * The actual "content" field sent to the API is constructed based on the {@code blocks} field during serialization.
 */
public class ChatMessage {
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
     * as that logic is handled during the {@link com.example.qwen_sdk.resources.ChatCompletion#buildRequestBody(ChatRequest)} process.
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
