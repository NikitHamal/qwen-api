package com.example.qwen_sdk.models;

/**
 * Represents a text content block within a multimodal message.
 * This class implements the {@link Block} interface.
 */
public class TextBlock implements Block {
    private String text;

    /**
     * Constructs a new TextBlock with the specified text content.
     *
     * @param text The text content for this block.
     */
    public TextBlock(String text) {
        this.text = text;
    }

    /**
     * Gets the text content of this block.
     *
     * @return The text content.
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text content of this block.
     *
     * @param text The new text content.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * {@inheritDoc}
     *
     * @return Always {@link BlockType#TEXT}.
     */
    @Override
    public BlockType getBlockType() {
        return BlockType.TEXT;
    }
}
