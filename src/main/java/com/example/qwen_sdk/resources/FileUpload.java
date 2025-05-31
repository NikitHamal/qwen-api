package com.example.qwen_sdk.resources;

import com.example.qwen_sdk.QwenClient;
import com.example.qwen_sdk.callback.QwenCallback;
import com.example.qwen_sdk.exception.QwenApiError;
import com.example.qwen_sdk.models.FileUploadResponse;

import com.google.gson.Gson;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * Handles File Upload operations for the Qwen API.
 * This class provides methods for uploading files (e.g., images for multimodal chat)
 * both synchronously and asynchronously.
 */
public class FileUpload {
    private final QwenClient qwenClient;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String fileUploadUrl;

    /**
     * Constructs a new FileUpload resource handler.
     *
     * @param qwenClient The {@link QwenClient} instance to use for API calls and configuration.
     * @throws IllegalArgumentException if qwenClient is null.
     */
    public FileUpload(QwenClient qwenClient) {
        if (qwenClient == null) {
            throw new IllegalArgumentException("QwenClient cannot be null.");
        }
        this.qwenClient = qwenClient;
        this.httpClient = qwenClient.getHttpClient();
        this.gson = qwenClient.getGson();
        // This URL is based on observations of the Python SDK. Adjust if official documentation differs.
        this.fileUploadUrl = qwenClient.getBaseUrl() + "/api/upload_file";
    }

    /**
     * Determines the MIME type of a file.
     * It first attempts to use {@link java.nio.file.Files#probeContentType(Path)}.
     * If that fails or returns null, it falls back to a simple extension-based lookup.
     *
     * @param fileName The name of the file, used for extension-based fallback.
     * @param file The {@link File} object itself, used for {@code probeContentType}.
     * @return The determined MIME type string, or "application/octet-stream" as a default.
     */
    private String determineMimeType(String fileName, File file) {
        if (file != null && file.exists()) {
            try {
                Path path = file.toPath();
                String mimeType = Files.probeContentType(path);
                if (mimeType != null) {
                    return mimeType;
                }
            } catch (IOException e) {
                qwenClient.getLogger().log(Level.WARNING, "Failed to probe MIME type for " + fileName + " using java.nio.file.Files.probeContentType. Falling back to extension.", e);
            } catch (SecurityException e) {
                 qwenClient.getLogger().log(Level.WARNING, "SecurityException while probing MIME type for " + fileName + ". Falling back to extension.", e);
            }
        }

        // Fallback to extension-based lookup
        if (fileName != null) {
            String lowerName = fileName.toLowerCase();
            if (lowerName.endsWith(".png")) return "image/png";
            if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) return "image/jpeg";
            if (lowerName.endsWith(".gif")) return "image/gif";
            if (lowerName.endsWith(".webp")) return "image/webp";
            if (lowerName.endsWith(".txt")) return "text/plain";
            // Add more common types as needed for your application
        }
        qwenClient.getLogger().info("Could not determine MIME type for " + fileName + " more accurately. Defaulting to application/octet-stream.");
        return "application/octet-stream"; // Default MIME type
    }

    /**
     * Uploads a file to the Qwen API synchronously.
     *
     * @param file The {@link File} to be uploaded. Must not be null and must exist.
     * @return A {@link FileUploadResponse} containing details about the uploaded file from the API.
     * @throws QwenApiError if the file does not exist, the API request fails, or an error occurs during processing.
     * @throws IllegalArgumentException if file is null.
     */
    public FileUploadResponse upload(File file) throws QwenApiError {
        if (file == null) {
            throw new IllegalArgumentException("File to upload cannot be null.");
        }
        if (!file.exists()) {
            throw new QwenApiError("File does not exist: " + file.getAbsolutePath(), new java.io.FileNotFoundException(file.getAbsolutePath()));
        }

        String mimeType = determineMimeType(file.getName(), file);
        qwenClient.getLogger().info("Uploading file: " + file.getName() + " with determined MIME type: " + mimeType);

        RequestBody fileBody = RequestBody.create(file, MediaType.parse(mimeType));
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                // The part name for the file is typically "file".
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(fileUploadUrl)
                .post(requestBody)
                .addHeader("Authorization", qwenClient.getAuthManager().getAuthorizationHeader())
                .addHeader("Cookie", qwenClient.getAuthManager().getCookieHeader())
                .addHeader("Accept", "application/json") // Expecting JSON response
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBodyStr = response.body() != null ? response.body().string() : "Unknown API error during file upload.";
                qwenClient.getLogger().severe("File Upload API Error: " + response.code() + " " + errorBodyStr);
                throw new QwenApiError("File upload failed with status code " + response.code(), response.code(), errorBodyStr);
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new QwenApiError("Empty response body from file upload API", response.code(), "Empty response body");
            }
            String responseJson = responseBody.string();
            qwenClient.getLogger().fine("File Upload Response JSON: " + responseJson);
            return gson.fromJson(responseJson, FileUploadResponse.class);
        } catch (IOException e) {
            qwenClient.getLogger().log(Level.SEVERE, "IOException during synchronous file upload", e);
            throw new QwenApiError("IOException during file upload API request: " + e.getMessage(), e);
        }
    }

    /**
     * Uploads a file to the Qwen API asynchronously.
     *
     * @param file The {@link File} to be uploaded.
     * @param callback The {@link QwenCallback} to handle the {@link FileUploadResponse} or failure.
     * @throws IllegalArgumentException if file or callback is null.
     */
    public void uploadAsync(final File file, final QwenCallback<FileUploadResponse> callback) {
        if (file == null) {
            throw new IllegalArgumentException("File to upload cannot be null.");
        }
         if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null for asynchronous operations.");
        }
        if (!file.exists()) {
            callback.onFailure(new QwenApiError("File does not exist: " + file.getAbsolutePath(), new java.io.FileNotFoundException(file.getAbsolutePath())));
            return;
        }

        String mimeType = determineMimeType(file.getName(), file);
        qwenClient.getLogger().info("Async uploading file: " + file.getName() + " with determined MIME type: " + mimeType);

        RequestBody fileBody = RequestBody.create(file, MediaType.parse(mimeType));
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(fileUploadUrl)
                .post(requestBody)
                .addHeader("Authorization", qwenClient.getAuthManager().getAuthorizationHeader())
                .addHeader("Cookie", qwenClient.getAuthManager().getCookieHeader())
                .addHeader("Accept", "application/json")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                qwenClient.getLogger().log(Level.SEVERE, "IOException during asynchronous file upload", e);
                callback.onFailure(new QwenApiError("IOException during file upload API request: " + e.getMessage(), e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        String errorBodyStr = responseBody != null ? responseBody.string() : "Unknown API error during file upload.";
                        qwenClient.getLogger().severe("File Upload API Error: " + response.code() + " " + errorBodyStr);
                        callback.onFailure(new QwenApiError("File upload failed with status code " + response.code(), response.code(), errorBodyStr));
                        return;
                    }
                    if (responseBody == null) {
                        callback.onFailure(new QwenApiError("Empty response body from file upload API", response.code(), "Empty response body"));
                        return;
                    }
                    String responseJson = responseBody.string();
                    qwenClient.getLogger().fine("File Upload Response JSON: " + responseJson);
                    FileUploadResponse fileUploadResponse = gson.fromJson(responseJson, FileUploadResponse.class);
                    callback.onSuccess(fileUploadResponse);
                } catch (Exception e) {
                    qwenClient.getLogger().log(Level.SEVERE, "Exception processing asynchronous file upload response", e);
                    callback.onFailure(new QwenApiError("Failed to process file upload API response: " + e.getMessage(), e));
                }
            }
        });
    }
}
