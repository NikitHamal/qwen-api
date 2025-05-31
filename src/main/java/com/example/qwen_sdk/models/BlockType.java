package com.example.qwen_sdk.models;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the type of content block within a multimodal chat message.
 */
public enum BlockType {
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
