package com.example.qwen_sdk.rag;

import com.example.qwen_sdk.QwenClient;
import com.example.qwen_sdk.callback.QwenCallback;
import com.example.qwen_sdk.exception.QwenApiError;
import com.example.qwen_sdk.models.ChatMessage;
import com.example.qwen_sdk.models.ChatRequest;
import com.example.qwen_sdk.models.ChatResponse;
import com.example.qwen_sdk.models.Role;
import com.example.qwen_sdk.models.TextBlock;
import com.example.qwen_sdk.models.Block;
import com.example.qwen_sdk.resources.ChatCompletion;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BasicResponseSynthesizer implements ResponseSynthesizer {

    private final QwenClient qwenClient;
    private final String promptTemplate;
    private final ChatCompletion chatCompletion; // Store reference

    public static final String DEFAULT_PROMPT_TEMPLATE =
            "Context information is below.\n" +
            "---------------------\n" +
            "{context_str}\n" +
            "---------------------\n" +
            "Given the context information and not prior knowledge, answer the query.\n" +
            "Query: {query_str}\n" +
            "Answer:";

    public BasicResponseSynthesizer(QwenClient qwenClient) {
        this(qwenClient, DEFAULT_PROMPT_TEMPLATE);
    }

    public BasicResponseSynthesizer(QwenClient qwenClient, String promptTemplate) {
        if (qwenClient == null) {
            throw new IllegalArgumentException("QwenClient cannot be null.");
        }
        if (promptTemplate == null || promptTemplate.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt template cannot be null or empty.");
        }
        this.qwenClient = qwenClient;
        this.promptTemplate = promptTemplate;
        this.chatCompletion = qwenClient.getChatCompletion(); // Get ChatCompletion instance
         if (this.chatCompletion == null) {
            throw new IllegalStateException("ChatCompletion resource not available from QwenClient.");
        }
    }

    @Override
    public ChatResponse synthesize(String query, List<Document> documents) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be null or empty.");
        }
        if (documents == null) { // Empty list is acceptable, null is not.
            throw new IllegalArgumentException("Documents list cannot be null.");
        }

        String contextStr = documents.stream()
                                   .map(doc -> doc.getContent() != null ? doc.getContent() : "")
                                   .collect(Collectors.joining("\n\n"));

        String formattedPrompt = promptTemplate
                                   .replace("{context_str}", contextStr)
                                   .replace("{query_str}", query);

        // Using the constructor ChatMessage(Role role, String textContent)
        ChatMessage userMessage = new ChatMessage(Role.USER, formattedPrompt);

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessages(Collections.singletonList(userMessage));

        String model = qwenClient.getDefaultModel();
        if (model == null || model.trim().isEmpty()){
             // Fallback or throw error if no sensible default can be obtained
            throw new IllegalStateException("Default model is not configured in QwenClient or is invalid.");
        }
        chatRequest.setModel(model);
        // Allow users to override other ChatRequest params if needed, e.g. temperature, by exposing setters or a builder.
        // For this basic synthesizer, we use defaults.

        try {
            return chatCompletion.create(chatRequest);
        } catch (QwenApiError e) {
            // Log or re-throw if specific handling is needed here
            // For now, let it propagate
            throw e;
        }
    }

    @Override
    public void synthesizeAsync(String query, List<Document> documents, QwenCallback<ChatResponse> callback) {
        if (query == null || query.trim().isEmpty()) {
            if (callback != null) {
                callback.onFailure(new QwenApiError("Query cannot be null or empty.", new IllegalArgumentException("Query is null or empty.")));
            }
            return;
        }
        if (documents == null) {
             if (callback != null) {
                callback.onFailure(new QwenApiError("Documents list cannot be null.", new IllegalArgumentException("Documents list is null.")));
            }
            return;
        }
         if (callback == null) {
            // Log or throw, similar to QueryEngine's async methods
            qwenClient.getLogger().warning("SynthesizeAsync called with null callback.");
            return;
        }


        String contextStr = documents.stream()
                                   .map(doc -> doc.getContent() != null ? doc.getContent() : "")
                                   .collect(Collectors.joining("\n\n"));

        String formattedPrompt = promptTemplate
                                   .replace("{context_str}", contextStr)
                                   .replace("{query_str}", query);

        ChatMessage userMessage = new ChatMessage(Role.USER, formattedPrompt);

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessages(Collections.singletonList(userMessage));

        String model = qwenClient.getDefaultModel();
        if (model == null || model.trim().isEmpty()){
            callback.onFailure(new QwenApiError("Default model is not configured or is invalid.", new IllegalStateException("Default model not configured.")));
            return;
        }
        chatRequest.setModel(model);

        chatCompletion.createAsync(chatRequest, callback);
    }
}
