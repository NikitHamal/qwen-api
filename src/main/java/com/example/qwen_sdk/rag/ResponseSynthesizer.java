package com.example.qwen_sdk.rag;

import com.example.qwen_sdk.callback.QwenCallback;
import com.example.qwen_sdk.models.ChatResponse;
import java.util.List;

public interface ResponseSynthesizer {
    ChatResponse synthesize(String query, List<Document> documents);
    void synthesizeAsync(String query, List<Document> documents, QwenCallback<ChatResponse> callback);
}
