package com.example.qwen_sdk.models;

import com.google.gson.annotations.SerializedName;

/**
 * Represents an image content block within a multimodal message.
 * This class implements the {@link Block} interface and includes a URL
 * from which the image can be fetched and its MIME type.
 */
public class ImageBlock implements Block {
    private String url;

    /**
     * The MIME type of the image (e.g., "image/png", "image/jpeg").
     * This field is serialized as "image_mimetype" in JSON.
     */
    @SerializedName("image_mimetype")
    private String imageMimetype;

    /**
     * Constructs a new ImageBlock with the specified image URL and MIME type.
     *
     * @param url The URL of the image. This could be a publicly accessible URL
     *            or a URL obtained from a file upload service (like Qwen's file upload).
     * @param imageMimetype The MIME type of the image (e.g., "image/png").
     */
    public ImageBlock(String url, String imageMimetype) {
        this.url = url;
        this.imageMimetype = imageMimetype;
    }

    /**
     * Gets the URL of the image.
     *
     * @return The image URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the image.
     *
     * @param url The new image URL.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the MIME type of the image.
     *
     * @return The image MIME type.
     */
    public String getImageMimetype() {
        return imageMimetype;
    }

    /**
     * Sets the MIME type of the image.
     *
     * @param imageMimetype The new image MIME type.
     */
    public void setImageMimetype(String imageMimetype) {
        this.imageMimetype = imageMimetype;
    }

    /**
     * {@inheritDoc}
     *
     * @return Always {@link BlockType#IMAGE}.
     */
    @Override
    public BlockType getBlockType() {
        return BlockType.IMAGE;
    }
}
