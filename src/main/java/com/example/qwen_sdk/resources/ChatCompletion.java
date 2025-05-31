package com.example.qwen_sdk.resources;

import com.example.qwen_sdk.QwenClient;
import com.example.qwen_sdk.callback.QwenCallback;
import com.example.qwen_sdk.exception.QwenApiError;
import com.example.qwen_sdk.models.ChatMessage;
import com.example.qwen_sdk.models.ChatRequest;
import com.example.qwen_sdk.models.ChatResponse;
import com.example.qwen_sdk.models.ChatResponseChunk;
import com.example.qwen_sdk.models.TextBlock;
import com.example.qwen_sdk.models.ImageBlock;
import com.example.qwen_sdk.models.Block;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

import java.io.BufferedReader;
import java.io.IOException;
// import java.io.InputStreamReader; // No longer needed for SseIterator with charStream
// import java.nio.charset.StandardCharsets; // No longer needed for SseIterator with charStream
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger; // Explicit import for SseIterator logger

/**
 * Handles Chat Completion operations for the Qwen API.
 * This class provides methods for creating synchronous, asynchronous, and streaming chat completions.
 */
public class ChatCompletion {
    private final QwenClient qwenClient;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final String completionsUrl;

    /**
     * Constructs a new ChatCompletion resource handler.
     *
     * @param qwenClient The {@link QwenClient} instance to use for API calls and configuration.
     * @throws IllegalArgumentException if qwenClient is null.
     */
    public ChatCompletion(QwenClient qwenClient) {
        if (qwenClient == null) {
            throw new IllegalArgumentException("QwenClient cannot be null.");
        }
        this.qwenClient = qwenClient;
        this.httpClient = qwenClient.getHttpClient();
        this.gson = qwenClient.getGson();
        this.completionsUrl = qwenClient.getBaseUrl() + "/v1/chat/completions";
    }

    /**
     * Builds the JSON request body for a chat completion request.
     * This method handles the custom serialization of {@link ChatMessage#blocks} into
     * the API-expected "content" field (string or array of typed blocks).
     *
     * @param chatRequest The {@link ChatRequest} object.
     * @return The {@link RequestBody} to be sent.
     */
    private RequestBody buildRequestBody(ChatRequest chatRequest) {
        JsonObject requestJson = gson.toJsonTree(chatRequest).getAsJsonObject();
        JsonArray messagesJson = requestJson.getAsJsonArray("messages");
        JsonArray newMessagesJson = new JsonArray();

        for (int i = 0; i < messagesJson.size(); i++) {
            JsonObject messageJson = messagesJson.get(i).getAsJsonObject();
            ChatMessage originalMessage = chatRequest.getMessages().get(i);

            if (originalMessage.getBlocks() != null && !originalMessage.getBlocks().isEmpty()) {
                if (originalMessage.getBlocks().size() == 1 && originalMessage.getBlocks().get(0) instanceof TextBlock) {
                    TextBlock textBlock = (TextBlock) originalMessage.getBlocks().get(0);
                    messageJson.add("content", new JsonPrimitive(textBlock.getText()));
                } else {
                    JsonArray contentBlocks = new JsonArray();
                    for (Block block : originalMessage.getBlocks()) {
                        JsonObject contentBlock = new JsonObject();
                        if (block instanceof TextBlock) {
                            contentBlock.addProperty("type", "text");
                            contentBlock.addProperty("text", ((TextBlock) block).getText());
                        } else if (block instanceof ImageBlock) {
                            // Qwen API for multimodal expects "image_url" with a nested "url"
                            contentBlock.addProperty("type", "image_url");
                            JsonObject imageUrlObject = new JsonObject();
                            imageUrlObject.addProperty("url", ((ImageBlock) block).getUrl());
                            contentBlock.add("image_url", imageUrlObject);
                        }
                        contentBlocks.add(contentBlock);
                    }
                    messageJson.add("content", contentBlocks);
                }
            }
            messageJson.remove("blocks");
            newMessagesJson.add(messageJson);
        }
        requestJson.add("messages", newMessagesJson);

        String json = gson.toJson(requestJson);
        qwenClient.getLogger().fine("Request JSON: " + json);
        return RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
    }

    /**
     * Creates a non-streaming chat completion.
     *
     * @param chatRequest The {@link ChatRequest} detailing the model, messages, and other parameters.
     * @return A {@link ChatResponse} containing the model's reply.
     * @throws QwenApiError if the API request fails or an error occurs during processing.
     * @throws IllegalArgumentException if chatRequest is null.
     */
    public ChatResponse create(ChatRequest chatRequest) throws QwenApiError {
        if (chatRequest == null) {
            throw new IllegalArgumentException("ChatRequest cannot be null.");
        }
        chatRequest.setStream(false);
        RequestBody body = buildRequestBody(chatRequest);
        Request request = new Request.Builder()
                .url(completionsUrl)
                .post(body)
                .addHeader("Authorization", qwenClient.getAuthManager().getAuthorizationHeader())
                .addHeader("Cookie", qwenClient.getAuthManager().getCookieHeader())
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBodyStr = response.body() != null ? response.body().string() : "Unknown API error";
                qwenClient.getLogger().severe("API Error: " + response.code() + " " + errorBodyStr);
                throw new QwenApiError("API request failed with status code " + response.code() + ": " + errorBodyStr, response.code(), errorBodyStr);
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new QwenApiError("Empty response body from API", response.code(), "Empty response body");
            }
            String responseJson = responseBody.string();
            qwenClient.getLogger().fine("Response JSON: " + responseJson);
            return gson.fromJson(responseJson, ChatResponse.class);
        } catch (IOException e) {
            qwenClient.getLogger().log(Level.SEVERE, "IOException during synchronous chat completion", e);
            throw new QwenApiError("IOException during API request: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a non-streaming chat completion asynchronously.
     *
     * @param chatRequest The {@link ChatRequest} detailing the model, messages, and other parameters.
     * @param callback The {@link QwenCallback} to handle the response or failure.
     * @throws IllegalArgumentException if chatRequest or callback is null.
     */
    public void createAsync(ChatRequest chatRequest, final QwenCallback<ChatResponse> callback) {
        if (chatRequest == null) {
            throw new IllegalArgumentException("ChatRequest cannot be null.");
        }
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null for asynchronous operations.");
        }
        chatRequest.setStream(false);
        RequestBody body = buildRequestBody(chatRequest);
        Request request = new Request.Builder()
                .url(completionsUrl)
                .post(body)
                .addHeader("Authorization", qwenClient.getAuthManager().getAuthorizationHeader())
                .addHeader("Cookie", qwenClient.getAuthManager().getCookieHeader())
                .addHeader("Accept", "application/json")
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                qwenClient.getLogger().log(Level.SEVERE, "IOException during asynchronous chat completion", e);
                callback.onFailure(new QwenApiError("IOException during API request: " + e.getMessage(), e));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        String errorBodyStr = responseBody != null ? responseBody.string() : "Unknown API error";
                        qwenClient.getLogger().severe("API Error: " + response.code() + " " + errorBodyStr);
                        callback.onFailure(new QwenApiError("API request failed with status code " + response.code() + ": " + errorBodyStr, response.code(), errorBodyStr));
                        return;
                    }
                    if (responseBody == null) {
                        callback.onFailure(new QwenApiError("Empty response body from API", response.code(), "Empty response body"));
                        return;
                    }
                    String responseJson = responseBody.string();
                    qwenClient.getLogger().fine("Response JSON: " + responseJson);
                    ChatResponse chatResponse = gson.fromJson(responseJson, ChatResponse.class);
                    callback.onSuccess(chatResponse);
                } catch (Exception e) {
                    qwenClient.getLogger().log(Level.SEVERE, "Exception processing asynchronous response", e);
                    callback.onFailure(new QwenApiError("Failed to process API response: " + e.getMessage(), e));
                }
            }
        });
    }

    /**
     * Creates a streaming chat completion, returning an {@link Iterable} of {@link ChatResponseChunk}.
     * This method makes a blocking HTTP call and then provides an iterator for consuming SSE events.
     *
     * @param chatRequest The {@link ChatRequest} set up for streaming.
     * @return An {@link Iterable} that yields {@link ChatResponseChunk} objects as they are received.
     * @throws QwenApiError if the initial API request fails.
     * @throws IllegalArgumentException if chatRequest is null.
     */
    public Iterable<ChatResponseChunk> createStream(ChatRequest chatRequest) throws QwenApiError {
        if (chatRequest == null) {
            throw new IllegalArgumentException("ChatRequest cannot be null.");
        }
        chatRequest.setStream(true); // Ensure stream is true
        RequestBody body = buildRequestBody(chatRequest);
        Request request = new Request.Builder()
                .url(completionsUrl)
                .post(body)
                .addHeader("Authorization", qwenClient.getAuthManager().getAuthorizationHeader())
                .addHeader("Cookie", qwenClient.getAuthManager().getCookieHeader())
                .addHeader("Accept", "text/event-stream")
                .build();

        try {
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                String errorBodyStr = response.body() != null ? response.body().string() : "Unknown API error";
                response.close();
                qwenClient.getLogger().severe("API Error for stream: " + response.code() + " " + errorBodyStr);
                throw new QwenApiError("API request failed for stream with status code " + response.code() + ": " + errorBodyStr, response.code(), errorBodyStr);
            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                response.close();
                throw new QwenApiError("Empty response body from API for stream", response.code(), "Empty response body");
            }
            // The SseIterator will be responsible for closing the responseBody's underlying stream (via reader.close())
            return () -> new SseIterator(responseBody.charStream(), gson, qwenClient.getLogger(), response);

        } catch (IOException e) {
            qwenClient.getLogger().log(Level.SEVERE, "IOException during synchronous stream chat completion", e);
            throw new QwenApiError("IOException during API stream request: " + e.getMessage(), e);
        }
    }

    /**
     * Iterator for processing Server-Sent Events (SSE) from a character stream.
     * Parses "data:" lines into {@link ChatResponseChunk} objects.
     */
    private static class SseIterator implements Iterator<ChatResponseChunk> {
        private final BufferedReader reader;
        private final Gson gson;
        private final Logger logger;
        private ChatResponseChunk nextChunk;
        private boolean finished = false;
        private final Response responseToClose; // Keep a reference to the Response to close it

        public SseIterator(java.io.Reader streamReader, Gson gson, Logger logger, Response responseToClose) {
            this.reader = new BufferedReader(streamReader);
            this.gson = gson;
            this.logger = logger;
            this.nextChunk = null;
            this.responseToClose = responseToClose;
        }

        @Override
        public boolean hasNext() {
            if (finished) return false;
            if (nextChunk != null) return true;

            try {
                String line;
                StringBuilder dataBuffer = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    logger.finest("SSE Line: " + line);
                    if (line.startsWith("data:")) {
                        dataBuffer.append(line.substring(5).trim());
                    } else if (line.isEmpty() && dataBuffer.length() > 0) {
                        String jsonData = dataBuffer.toString();
                        logger.fine("SSE Data JSON: " + jsonData);
                        if ("[DONE]".equalsIgnoreCase(jsonData.trim())) {
                            finished = true;
                            closeResources();
                            return false;
                        }
                        try {
                            nextChunk = gson.fromJson(jsonData, ChatResponseChunk.class);
                            if (nextChunk != null) {
                                return true;
                            }
                        } catch (com.google.gson.JsonSyntaxException e) {
                            logger.log(Level.WARNING, "Failed to parse SSE JSON: " + jsonData, e);
                        }
                        dataBuffer.setLength(0);
                    }
                }
                finished = true;
                closeResources();
                return false;
            } catch (IOException e) {
                logger.log(Level.SEVERE, "IOException reading SSE stream", e);
                finished = true;
                closeResources();
                return false;
            }
        }

        @Override
        public ChatResponseChunk next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more chunks in SSE stream.");
            }
            ChatResponseChunk chunkToReturn = nextChunk;
            nextChunk = null;
            return chunkToReturn;
        }

        private void closeResources() {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing SSE stream reader", e);
            }
            if (responseToClose != null) {
                responseToClose.close(); // Close the OkHttp Response
            }
        }
    }

    /**
     * Creates a streaming chat completion asynchronously using OkHttp's EventSource.
     *
     * @param chatRequest The {@link ChatRequest} set up for streaming.
     * @param chunkCallback The {@link QwenCallback} to handle each incoming {@link ChatResponseChunk}.
     * @param completionCallback The {@link QwenCallback} to signal the completion or failure of the entire stream.
     * @throws IllegalArgumentException if chatRequest, chunkCallback, or completionCallback is null.
     */
    public void createStreamAsync(ChatRequest chatRequest,
                                  final QwenCallback<ChatResponseChunk> chunkCallback,
                                  final QwenCallback<Void> completionCallback) {
        if (chatRequest == null) {
            throw new IllegalArgumentException("ChatRequest cannot be null.");
        }
        if (chunkCallback == null) {
            throw new IllegalArgumentException("ChunkCallback cannot be null for asynchronous streaming.");
        }
         if (completionCallback == null) {
            throw new IllegalArgumentException("CompletionCallback cannot be null for asynchronous streaming.");
        }

        chatRequest.setStream(true); // Ensure stream is true
        RequestBody body = buildRequestBody(chatRequest);
        Request request = new Request.Builder()
                .url(completionsUrl)
                .post(body)
                .addHeader("Authorization", qwenClient.getAuthManager().getAuthorizationHeader())
                .addHeader("Cookie", qwenClient.getAuthManager().getCookieHeader())
                .addHeader("Accept", "text/event-stream")
                .addHeader("Cache-Control", "no-cache") // Recommended for SSE
                .build();

        EventSourceListener listener = new EventSourceListener() {
            @Override
            public void onOpen(EventSource eventSource, Response response) {
                qwenClient.getLogger().info("SSE connection opened.");
            }

            @Override
            public void onEvent(EventSource eventSource, String id, String type, String data) {
                qwenClient.getLogger().fine("SSE Event: id=" + id + ", type=" + type + ", data=" + data);
                if ("[DONE]".equalsIgnoreCase(data.trim())) {
                    // OkHttp's EventSource listener's onClosed or onFailure will handle stream termination.
                    // No explicit action needed here for [DONE] if it comes as regular data.
                    return;
                }
                try {
                    ChatResponseChunk chunk = gson.fromJson(data, ChatResponseChunk.class);
                    chunkCallback.onSuccess(chunk);
                } catch (com.google.gson.JsonSyntaxException e) {
                    qwenClient.getLogger().log(Level.WARNING, "Failed to parse SSE JSON data: " + data, e);
                    // Optionally, notify via chunkCallback.onFailure for this specific chunk error
                    // chunkCallback.onFailure(new QwenApiError("Failed to parse SSE chunk: " + data, e));
                }
            }

            @Override
            public void onClosed(EventSource eventSource) {
                qwenClient.getLogger().info("SSE connection closed by server.");
                completionCallback.onSuccess(null);
            }

            @Override
            public void onFailure(EventSource eventSource, Throwable t, Response response) {
                String errorMessage = "SSE connection failed";
                int statusCode = 0;
                String errorBodyDetails = "";

                if (t != null) {
                    errorMessage += ": " + t.getMessage();
                    qwenClient.getLogger().log(Level.SEVERE, "SSE failure", t);
                }
                if (response != null) {
                    statusCode = response.code();
                    errorMessage += " (status code: " + statusCode + ")";
                    try (ResponseBody responseBody = response.body()){ // Ensure response body is closed
                        if (responseBody != null) {
                           errorBodyDetails = responseBody.string();
                           // Avoid logging potentially large/sensitive error bodies directly unless in DEBUG
                           qwenClient.getLogger().warning(errorMessage + (errorBodyDetails.isEmpty() ? "" : " - Check details if available."));
                        }
                    } catch (IOException e) {
                         qwenClient.getLogger().log(Level.WARNING, "Error reading error response body in SSE failure", e);
                    } finally {
                        if(response != null) response.close(); // Ensure response is closed
                    }
                } else if (t instanceof IOException) { // Network error before response
                     qwenClient.getLogger().log(Level.SEVERE, "SSE network failure", t);
                }

                QwenApiError apiError = new QwenApiError(errorMessage + (t != null ? " Cause: " + t.toString() : ""), statusCode, errorBodyDetails);
                if(t != null && !(t instanceof IOException)) apiError = new QwenApiError(errorMessage, t);


                completionCallback.onFailure(apiError);
            }
        };

        EventSources.createFactory(this.httpClient).newEventSource(request, listener);
        qwenClient.getLogger().info("Async SSE request initiated.");
    }
}
