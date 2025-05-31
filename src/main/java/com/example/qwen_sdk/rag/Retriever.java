package com.example.qwen_sdk.rag;

import com.example.qwen_sdk.callback.QwenCallback;
import java.util.List;

public interface Retriever {
    List<Document> retrieve(String query);
    void retrieveAsync(String query, QwenCallback<List<Document>> callback);
}
