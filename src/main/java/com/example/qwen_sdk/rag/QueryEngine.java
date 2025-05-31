package com.example.qwen_sdk.rag;

import com.example.qwen_sdk.callback.QwenCallback;
import com.example.qwen_sdk.exception.QwenApiError; // Corrected import
import com.example.qwen_sdk.models.ChatResponse;
import java.util.List;
// Potential future import if async chaining is fully built out here
// import java.util.concurrent.CompletableFuture;

public class QueryEngine {
    private final Retriever retriever;
    private final ResponseSynthesizer responseSynthesizer;

    public QueryEngine(Retriever retriever, ResponseSynthesizer responseSynthesizer) {
        if (retriever == null) {
            throw new IllegalArgumentException("Retriever cannot be null.");
        }
        if (responseSynthesizer == null) {
            throw new IllegalArgumentException("ResponseSynthesizer cannot be null.");
        }
        this.retriever = retriever;
        this.responseSynthesizer = responseSynthesizer;
    }

    public ChatResponse query(String userQuery) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("User query cannot be null or empty.");
        }
        List<Document> documents = retriever.retrieve(userQuery);
        return responseSynthesizer.synthesize(userQuery, documents);
    }

    public void queryAsync(String userQuery, final QwenCallback<ChatResponse> callback) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            if (callback != null) {
                callback.onFailure(new QwenApiError("User query cannot be null or empty.",
                                   new IllegalArgumentException("User query cannot be null or empty.")));
            }
            return;
        }
        if (callback == null) {
            // Or log a warning, or throw an exception if callback is mandatory for async.
            // Depending on API design choice. For now, let it proceed but it won't notify anyone.
            // Consider throwing IllegalArgumentException("Callback cannot be null for async operations.");
            return;
        }

        retriever.retrieveAsync(userQuery, new QwenCallback<List<Document>>() {
            @Override
            public void onSuccess(List<Document> documents) {
                responseSynthesizer.synthesizeAsync(userQuery, documents, callback);
            }

            @Override
            public void onFailure(QwenApiError error) { // Corrected type
                callback.onFailure(error);
            }
        });
    }
}
