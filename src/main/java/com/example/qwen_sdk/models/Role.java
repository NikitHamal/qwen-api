package com.example.qwen_sdk.models;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the role of the message sender in a chat conversation.
 */
public enum Role {
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
