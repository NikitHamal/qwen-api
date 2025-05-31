package com.example.qwen_sdk.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * Represents a message received from the Qwen API in a chat response.
 * This can be a complete message or a delta in a streamed response.
 */
public class ResponseMessage {
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
