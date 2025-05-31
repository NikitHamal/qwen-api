package com.example.qwen_sdk.rag;

import java.util.Map;

public interface Document {
    String getId();
    String getContent();
    Map<String, Object> getMetadata();
}
