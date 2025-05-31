package com.example.qwen_sdk.models;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the response received from the Qwen API after a successful file upload.
 * It typically includes URLs, IDs, and metadata about the uploaded file.
 * This model is based on observations from the Python SDK's `FileData` object.
 */
public class FileUploadResponse {

    /**
     * The URL where the uploaded file can be accessed, often a temporary or CDN URL.
     * Serialized as "file_url".
     */
    @SerializedName("file_url")
    private String fileUrl;

    /**
     * The MIME type of the uploaded image. This field might be specific to image uploads
     * and could be null or absent for other file types.
     * Serialized as "image_mimetype".
     */
    @SerializedName("image_mimetype")
    private String imageMimetype;

    /**
     * The unique identifier assigned to the uploaded file by the Qwen API.
     * Serialized as "file_id".
     */
    @SerializedName("file_id")
    private String fileId;

    /**
     * The name of the file as it was uploaded or as stored by the server.
     * Serialized as "file_name".
     */
    @SerializedName("file_name")
    private String fileName;

    /**
     * Default constructor, primarily for use by Gson during deserialization.
     */
    public FileUploadResponse() {}

    /**
     * Constructs a FileUploadResponse with all specified fields.
     *
     * @param fileUrl The URL of the uploaded file.
     * @param imageMimetype The MIME type of the image (can be null).
     * @param fileId The unique ID of the file.
     * @param fileName The name of the file.
     */
    public FileUploadResponse(String fileUrl, String imageMimetype, String fileId, String fileName) {
        this.fileUrl = fileUrl;
        this.imageMimetype = imageMimetype;
        this.fileId = fileId;
        this.fileName = fileName;
    }

    /**
     * Gets the URL of the uploaded file.
     * @return The file URL.
     */
    public String getFileUrl() {
        return fileUrl;
    }

    /**
     * Sets the URL of the uploaded file.
     * @param fileUrl The file URL.
     */
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    /**
     * Gets the MIME type of the uploaded image.
     * @return The image MIME type, or null if not applicable/available.
     */
    public String getImageMimetype() {
        return imageMimetype;
    }

    /**
     * Sets the MIME type of the uploaded image.
     * @param imageMimetype The image MIME type.
     */
    public void setImageMimetype(String imageMimetype) {
        this.imageMimetype = imageMimetype;
    }

    /**
     * Gets the unique ID of the uploaded file.
     * @return The file ID.
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * Sets the unique ID of the uploaded file.
     * @param fileId The file ID.
     */
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    /**
     * Gets the name of the uploaded file.
     * @return The file name.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the name of the uploaded file.
     * @param fileName The file name.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Returns a string representation of the FileUploadResponse object.
     * @return A string containing the values of the fields.
     */
    @Override
    public String toString() {
        return "FileUploadResponse{" +
               "fileUrl='" + fileUrl + '\'' +
               ", imageMimetype='" + imageMimetype + '\'' +
               ", fileId='" + fileId + '\'' +
               ", fileName='" + fileName + '\'' +
               '}';
    }
}
