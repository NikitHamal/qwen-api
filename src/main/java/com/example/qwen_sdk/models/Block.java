package com.example.qwen_sdk.models;

/**
 * Represents a generic content block in a multimodal message.
 * Implementations of this interface define specific types of content, like text or images.
 */
public interface Block {
    /**
     * Gets the type of this content block.
     *
     * @return The {@link BlockType} enum value indicating the nature of the block.
     */
    BlockType getBlockType();
}
