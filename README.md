# Qwen Java SDK

A community-driven Java SDK for interacting with the Alibaba Cloud Tongyi Qwen Large Language Models (LLMs). This SDK provides a convenient way to integrate Qwen's powerful capabilities into your Java applications.

## Features

*   **Chat Completions:**
    *   Synchronous and asynchronous API calls.
    *   Streaming support for real-time responses.
    *   Support for multimodal chat (text and images).
*   **File Uploads:**
    *   Upload files (e.g., images) for use with vision-capable models.
    *   Synchronous and asynchronous uploads.
*   **RAG Framework (Conceptual):**
    *   Interfaces and basic classes for building Retrieval Augmented Generation pipelines (`Document`, `Retriever`, `ResponseSynthesizer`, `QueryEngine`).
*   **Ease of Use:**
    *   Fluent API design.
    *   Helper classes for authentication and request building.
    *   Comprehensive Javadoc documentation.
*   **Extensibility:**
    *   Designed to be adaptable for new Qwen API features.

## Installation

### Gradle (Recommended for most Java/Android projects)

Add the following dependencies to your `build.gradle` file:

```gradle
dependencies {
    // Assuming the SDK is published to Maven Central or a similar repository
    // implementation 'com.example:qwen-sdk:1.0.0' // Replace with actual coordinates and version

    // Required transitive dependencies (if not automatically pulled by the SDK)
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'      // Or the version used by the SDK
    implementation 'com.squareup.okhttp3:okhttp-sse:4.12.0' // For streaming
    implementation 'com.google.code.gson:gson:2.11.0'        // Or the version used by the SDK
}
```

*If the SDK is not yet on a public repository, you'll need to build the JAR locally and include it. See "Local JAR integration" below.*

### Local JAR integration (e.g., for projects not using Gradle directly for this SDK)
1.  Build the JAR from source (e.g., using `./gradlew jar` if the SDK project is Gradle-based).
2.  Add the generated `qwen-sdk.jar` to your project's `libs` folder.
3.  Ensure your build system includes this JAR in the classpath. For Gradle, this means adding:
    ```gradle
    implementation files('libs/qwen-sdk.jar') // Adjust path if needed
    // Also add OkHttp, Gson, OkHttp-SSE dependencies as shown above
    ```

---
## Qwen API Credential Setup

To use this SDK, you need access to the Qwen API, which typically requires an API Key and often a specific Cookie string for authentication.

1.  **API Key (SK_API_KEY):**
    *   Obtain your API key from the Alibaba Cloud console or the relevant Qwen platform.
2.  **Cookie:**
    *   For some Qwen services, particularly those accessed via web interfaces or specific API endpoints, a session cookie might be required. This cookie is usually obtained by logging into the Qwen web platform and inspecting browser cookies.
    *   **Note:** Cookie-based authentication can be less stable as cookies expire. Prefer token-based authentication if the API supports it for your use case. This SDK's `AuthManager` primarily supports API Key (as Bearer token) and a general Cookie header.

**Security Best Practices:**
*   **Never hardcode your API Key or Cookie directly in client-side application code that will be distributed.**
*   **Server-Side Applications:** Use environment variables or secure configuration management services.
*   **Client-Side/Mobile Applications (like Android):**
    *   Your application should ideally communicate with a backend server you control. This backend server then makes calls to the Qwen API using securely stored credentials.
    *   If direct client-side calls are unavoidable (e.g., for Sketchware Pro examples or testing):
        *   Prompt the user to enter their API Key/Cookie.
        *   Store these credentials securely on the device using Android's `SharedPreferences` with encryption (e.g., using `androidx.security.crypto.EncryptedSharedPreferences`).
        *   Be transparent with users about how and why their credentials are stored and used.

---
## Usage Examples

### 1. Initialization

First, initialize the `QwenClient` with your API key and cookie.

```java
import com.example.qwen_sdk.QwenClient;

// Replace with your actual API key and cookie or load them securely
String apiKey = "YOUR_SK_API_KEY";
String apiCookie = "YOUR_QWEN_COOKIE";

// Basic initialization (uses default base URL, timeout, and model "qwen-turbo")
QwenClient qwenClient = new QwenClient(apiKey, apiCookie);

// Or with more options:
// String baseUrl = "https://chat.qwen.ai"; // Or your specific endpoint
// int timeoutSeconds = 60;
// String loggingLevel = "INFO"; // e.g., "DEBUG", "INFO", "WARNING"
// boolean saveLogs = false; // Placeholder for future log saving feature
// String defaultModel = "qwen-plus"; // Default model for requests
// QwenClient qwenClientWithOptions = new QwenClient(apiKey, apiCookie, baseUrl, timeoutSeconds, loggingLevel, saveLogs, defaultModel);
```

### 2. Basic Chat Completion (Non-Streaming)

```java
import com.example.qwen_sdk.models.ChatMessage;
import com.example.qwen_sdk.models.ChatRequest;
import com.example.qwen_sdk.models.ChatResponse;
import com.example.qwen_sdk.models.Role;
import com.example.qwen_sdk.callback.QwenCallback;
import com.example.qwen_sdk.exception.QwenApiError;
import java.util.ArrayList;
import java.util.List;

// --- Synchronous Call ---
List<ChatMessage> messages = new ArrayList<>();
messages.add(new ChatMessage(Role.SYSTEM, "You are a helpful assistant."));
messages.add(new ChatMessage(Role.USER, "Hello, what is the capital of France?"));

ChatRequest request = new ChatRequest(qwenClient.getDefaultModel(), messages);
// You can also specify a model directly: request.setModel("qwen-plus");
// Optional: request.setTemperature(0.7f);

try {
    ChatResponse response = qwenClient.getChatCompletion().create(request);
    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
        System.out.println("Sync Response: " + response.getChoices().get(0).getMessage().getContent());
    }
} catch (QwenApiError e) {
    System.err.println("Sync API Error: " + e.getMessage() + " (Code: " + e.getStatusCode() + ", Details: " + e.getErrorMessage() + ")");
}

// --- Asynchronous Call ---
qwenClient.getChatCompletion().createAsync(request, new QwenCallback<ChatResponse>() {
    @Override
    public void onSuccess(ChatResponse response) {
        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            System.out.println("Async Response: " + response.getChoices().get(0).getMessage().getContent());
        }
    }

    @Override
    public void onFailure(QwenApiError error) {
        System.err.println("Async API Error: " + error.getMessage() + " (Code: " + error.getStatusCode() + ", Details: " + error.getErrorMessage() + ")");
    }
});
```

### 3. Streaming Chat Completion

```java
import com.example.qwen_sdk.models.ChatMessage;
import com.example.qwen_sdk.models.ChatRequest;
import com.example.qwen_sdk.models.ChatResponseChunk;
import com.example.qwen_sdk.models.Role;
import com.example.qwen_sdk.callback.QwenCallback;
import com.example.qwen_sdk.exception.QwenApiError;
import java.util.ArrayList;
import java.util.List;

List<ChatMessage> messagesStream = new ArrayList<>();
messagesStream.add(new ChatMessage(Role.USER, "Tell me a short story about a brave robot."));

ChatRequest streamRequest = new ChatRequest(qwenClient.getDefaultModel(), messagesStream);

// --- Synchronous Stream (Iterable) ---
// Note: This blocks the current thread for the initial HTTP connection, then iterates.
System.out.println("\n--- Synchronous Stream ---");
try {
    Iterable<ChatResponseChunk> streamIterable = qwenClient.getChatCompletion().createStream(streamRequest);
    for (ChatResponseChunk chunk : streamIterable) {
        if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
            String content = chunk.getChoices().get(0).getDelta().getContent();
            if (content != null) {
                System.out.print(content);
            }
        }
    }
    System.out.println("\nSync Stream Ended.");
} catch (QwenApiError e) {
    System.err.println("Sync Stream API Error: " + e.getMessage());
}


// --- Asynchronous Stream ---
System.out.println("\n--- Asynchronous Stream ---");
qwenClient.getChatCompletion().createStreamAsync(streamRequest,
    new QwenCallback<ChatResponseChunk>() { // Chunk callback
        @Override
        public void onSuccess(ChatResponseChunk chunk) {
            if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
                String content = chunk.getChoices().get(0).getDelta().getContent();
                if (content != null) {
                    System.out.print(content); // Append content as it arrives
                }
            }
        }

        @Override
        public void onFailure(QwenApiError error) {
            // This callback might be called for individual chunk processing errors,
            // though less common. Terminal errors are usually on completionCallback.
            System.err.println("\nAsync Stream Chunk Error: " + error.getMessage());
        }
    },
    new QwenCallback<Void>() { // Completion callback
        @Override
        public void onSuccess(Void response) {
            System.out.println("\nAsync Stream Ended.");
        }

        @Override
        public void onFailure(QwenApiError error) {
            System.err.println("\nAsync Stream Failure: " + error.getMessage() + " (Code: " + error.getStatusCode() + ", Details: " + error.getErrorMessage() + ")");
        }
    }
);

// Keep the main thread alive for async example to complete in a simple console app
// try { Thread.sleep(10000); } catch (InterruptedException e) { e.printStackTrace(); }
```

### 4. File Upload and Chat with Image (Vision Model)

```java
import com.example.qwen_sdk.models.FileUploadResponse;
import com.example.qwen_sdk.models.Block;
import com.example.qwen_sdk.models.ImageBlock;
import com.example.qwen_sdk.models.TextBlock;
import com.example.qwen_sdk.models.ChatMessage;
import com.example.qwen_sdk.models.ChatRequest;
import com.example.qwen_sdk.models.ChatResponse;
import com.example.qwen_sdk.models.Role;
import com.example.qwen_sdk.callback.QwenCallback;
import com.example.qwen_sdk.exception.QwenApiError;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Assume 'qwenClient' is already initialized

// Path to your image file
String imagePath = "path/to/your/image.png"; // Replace with a valid image path
File imageFile = new File(imagePath);

if (!imageFile.exists()) {
    System.err.println("Image file not found: " + imagePath);
    // return; // or handle error
}

// --- Asynchronous File Upload ---
System.out.println("Uploading file: " + imageFile.getName());
qwenClient.getFileUpload().uploadAsync(imageFile, new QwenCallback<FileUploadResponse>() {
    @Override
    public void onSuccess(FileUploadResponse uploadResponse) {
        System.out.println("File Uploaded Successfully!");
        System.out.println("File ID: " + uploadResponse.getFileId());
        System.out.println("File URL: " + uploadResponse.getFileUrl());

        // Now, chat with the uploaded image
        List<Block> contentBlocks = new ArrayList<>();
        contentBlocks.add(new ImageBlock(uploadResponse.getFileUrl(), uploadResponse.getImageMimetype()));
        contentBlocks.add(new TextBlock("What is in this image?"));

        ChatMessage visionMessage = new ChatMessage(Role.USER, contentBlocks);
        List<ChatMessage> visionMessages = new ArrayList<>();
        visionMessages.add(visionMessage);

        // IMPORTANT: Use a vision-capable model
        ChatRequest visionRequest = new ChatRequest("qwen-vl-plus", visionMessages);
        // Or another model like "qwen-vl-max"

        qwenClient.getChatCompletion().createAsync(visionRequest, new QwenCallback<ChatResponse>() {
            @Override
            public void onSuccess(ChatResponse chatResponse) {
                if (chatResponse.getChoices() != null && !chatResponse.getChoices().isEmpty()) {
                    System.out.println("Vision Model Response: " + chatResponse.getChoices().get(0).getMessage().getContent());
                }
            }

            @Override
            public void onFailure(QwenApiError error) {
                System.err.println("Vision Chat API Error: " + error.getMessage());
            }
        });
    }

    @Override
    public void onFailure(QwenApiError error) {
        System.err.println("File Upload API Error: " + error.getMessage());
    }
});

// Keep the main thread alive for async example
// try { Thread.sleep(20000); } catch (InterruptedException e) { e.printStackTrace(); }
```

### 5. RAG (Retrieval Augmented Generation) - Conceptual

This SDK provides interfaces for building RAG pipelines. You would need to implement the `Retriever` and potentially customize the `ResponseSynthesizer`.

```java
import com.example.qwen_sdk.rag.Document;
import com.example.qwen_sdk.rag.Retriever;
import com.example.qwen_sdk.rag.ResponseSynthesizer;
import com.example.qwen_sdk.rag.QueryEngine;
import com.example.qwen_sdk.rag.BasicResponseSynthesizer; // Example implementation
import com.example.qwen_sdk.models.ChatResponse;
import com.example.qwen_sdk.callback.QwenCallback; // For async
import com.example.qwen_sdk.exception.QwenApiError; // For async error
import java.util.List;
import java.util.Map;
import java.util.ArrayList; // For dummy retriever

// 1. Implement your Retriever
class MySimpleRetriever implements Retriever {
    private List<Document> documents;

    public MySimpleRetriever(List<Document> documents) {
        this.documents = documents;
    }

    @Override
    public List<Document> retrieve(String query) {
        // Simple keyword matching for demonstration
        List<Document> relevantDocs = new ArrayList<>();
        for (Document doc : documents) {
            if (doc.getContent().toLowerCase().contains(query.toLowerCase())) {
                relevantDocs.add(doc);
            }
        }
        System.out.println("Retrieved " + relevantDocs.size() + " documents for query: " + query);
        return relevantDocs;
    }

    @Override
    public void retrieveAsync(String query, QwenCallback<List<Document>> callback) {
        // Simple async wrapper for demonstration
        try {
            callback.onSuccess(retrieve(query));
        } catch (Exception e) {
            callback.onFailure(new QwenApiError("Retrieval failed: " + e.getMessage(), e));
        }
    }
}

// 2. Implement your Document class (or use a generic one if provided)
class MyDocument implements Document {
    private String id;
    private String content;
    private Map<String, Object> metadata;

    public MyDocument(String id, String content, Map<String, Object> metadata) {
        this.id = id;
        this.content = content;
        this.metadata = metadata;
    }
    @Override public String getId() { return id; }
    @Override public String getContent() { return content; }
    @Override public Map<String, Object> getMetadata() { return metadata; }
}


// 3. Setup QwenClient (as shown in initialization)
// QwenClient qwenClient = new QwenClient("YOUR_API_KEY", "YOUR_COOKIE", "qwen-max"); // Use a powerful model for RAG

// 4. Create some documents
List<Document> myDocuments = new ArrayList<>();
myDocuments.add(new MyDocument("doc1", "The Eiffel Tower is located in Paris, France.", null));
myDocuments.add(new MyDocument("doc2", "The capital of Japan is Tokyo.", null));
myDocuments.add(new MyDocument("doc3", "The Qwen LLM supports multiple languages.", null));


// 5. Initialize Retriever and ResponseSynthesizer
Retriever retriever = new MySimpleRetriever(myDocuments);
// Using the BasicResponseSynthesizer provided by the SDK
ResponseSynthesizer synthesizer = new BasicResponseSynthesizer(qwenClient);
// Or a custom one: ResponseSynthesizer synthesizer = new MyCustomSynthesizer(qwenClient);

// 6. Create QueryEngine
QueryEngine queryEngine = new QueryEngine(retriever, synthesizer);

// 7. Query
String userQuery = "Where is the Eiffel Tower?";
System.out.println("\nQuerying RAG Engine for: " + userQuery);
try {
    ChatResponse ragResponse = queryEngine.query(userQuery);
    if (ragResponse.getChoices() != null && !ragResponse.getChoices().isEmpty()) {
        System.out.println("RAG Response: " + ragResponse.getChoices().get(0).getMessage().getContent());
    }
} catch (QwenApiError e) {
    System.err.println("RAG Query Error: " + e.getMessage());
}

// Example with a different query
userQuery = "Tell me about Qwen LLM.";
System.out.println("\nQuerying RAG Engine for: " + userQuery);
try {
    ChatResponse ragResponse = queryEngine.query(userQuery);
    if (ragResponse.getChoices() != null && !ragResponse.getChoices().isEmpty()) {
        System.out.println("RAG Response: " + ragResponse.getChoices().get(0).getMessage().getContent());
    }
} catch (QwenApiError e) {
    System.err.println("RAG Query Error: " + e.getMessage());
}
```

---
## Sketchware Pro Integration

This section provides specific guidance for using the Qwen Java SDK within Sketchware Pro projects.

### Setup Instructions for Sketchware Pro

Follow these steps to integrate the Qwen SDK into your Sketchware Pro project.

**1. Library Integration:**

   *   **Create the SDK JAR:**
        *   If you have the SDK source code as a separate Gradle project, you can typically create a JAR file by running the command `./gradlew jar` in the root of that SDK project. This will compile the code and package it into a JAR file (usually found in `build/libs/`).
        *   Alternatively, if you have compiled `.class` files, you can manually create a JAR using the `jar` command-line tool: `jar cvf qwen-sdk.jar -C path/to/compiled/classes .`

   *   **Place the JAR in Sketchware Pro:**
        *   Copy the generated `qwen-sdk.jar` (or whatever you named it) into your Sketchware Pro project's local library directory. This is typically `libs/` inside your app's structure that Sketchware Pro uses. (You might need to use Sketchware Pro's "Local Library" manager if it provides one, or manually place it if you have access to the project's file structure).

   *   **Add Dependencies to `build.gradle`:**
        *   Sketchware Pro allows editing the `build.gradle` file for your project. You'll need to add dependencies for OkHttp, Gson, and OkHttp's SSE module.
        *   Open your project in Sketchware Pro, navigate to the `build.gradle` (module: app) editor.
        *   Add the following to your `dependencies` block:

        ```gradle
        dependencies {
            // ... other dependencies ...

            // Qwen SDK JAR (if placed in libs folder)
            implementation files('libs/qwen-sdk.jar') // Adjust path if needed

            // OkHttp for networking
            implementation 'com.squareup.okhttp3:okhttp:4.12.0'
            // OkHttp SSE for streaming
            implementation 'com.squareup.okhttp3:okhttp-sse:4.12.0' // Use same version as okhttp
            // Gson for JSON parsing
            implementation 'com.google.code.gson:gson:2.11.0'

            // ... other dependencies ...
        }
        ```
        *Note: Ensure version numbers are up-to-date or match what your SDK was built against.*

**2. Permissions:**

   *   Add the following permissions to your `AndroidManifest.xml` file. Sketchware Pro usually provides an interface to add permissions.
        *   `INTERNET`: Required for making network calls to the Qwen API.
        *   `READ_EXTERNAL_STORAGE`: Required if you are picking files from shared storage for upload (especially on older Android versions). For Android 10+ and scoped storage, specific file access mechanisms might be used which might not always require this broad permission if using `ACTION_OPEN_DOCUMENT`.
        *   `WRITE_EXTERNAL_STORAGE`: (Optional) Only if your application needs to save files publicly. Not directly needed for SDK operation unless saving logs to public storage.

**3. API Key/Cookie Management:**

   *   **Security:** Never hardcode your API key or full cookie directly in your publicly distributed app's Java code if avoidable.
   *   **Storage:**
        *   For user-provided keys/cookies: Store them securely using Android's `SharedPreferences` after the user inputs them.
        *   For keys you embed (not recommended for client-side apps): If absolutely necessary and for private use, you might place them in code, but be aware of the risks. Obfuscation can help but isn't foolproof.
   *   **Example (Storing in SharedPreferences):**
        ```java
        // To save
        // Assuming 'this' is an Activity or Context
        SharedPreferences prefs = this.getSharedPreferences("QwenPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("apiKey", "YOUR_API_KEY");
        editor.putString("apiCookie", "YOUR_COOKIE_STRING");
        editor.apply();

        // To retrieve (in your Activity's onCreate or similar)
        // SharedPreferences prefs = this.getSharedPreferences("QwenPrefs", MODE_PRIVATE);
        // String apiKey = prefs.getString("apiKey", null);
        // String apiCookie = prefs.getString("apiCookie", null);
        ```

**4. ProGuard / R8 Rules:**

   *   If you are using ProGuard or R8 for code shrinking and obfuscation (often enabled for release builds in Sketchware Pro), you need to add rules to prevent issues with libraries like OkHttp, Gson, and potentially your SDK classes if they use reflection heavily.
   *   Add these rules to your `proguard-rules.pro` file (Sketchware Pro should have a section for this):

    ```proguard
    # For OkHttp
    -keep class okhttp3.** { *; }
    -keep interface okhttp3.** { *; }
    -dontwarn okhttp3.**
    -dontwarn okio.**

    # For OkHttp SSE
    -keep class okhttp3.sse.** { *; }
    -keep interface okhttp3.sse.** { *; }

    # For Gson
    -keep class com.google.gson.** { *; }
    -keepattributes Signature
    -keepattributes InnerClasses
    # For your SDK's data model classes (POJOs) if they are serialized/deserialized by Gson
    # Replace com.example.qwen_sdk.models.** with your actual package name
    -keep class com.example.qwen_sdk.models.** { *; }
    -keep public class * extends com.google.gson.TypeAdapter

    # Keep your SDK classes (adjust package name as needed)
    -keep class com.example.qwen_sdk.** { *; }
    -keep interface com.example.qwen_sdk.** { *; }
    -keep interface com.example.qwen_sdk.rag.** { *; } # If using RAG
    -keep class com.example.qwen_sdk.rag.** { *; }    # If using RAG

    # Keep annotations used by Gson
    -keepattributes *Annotation*

    # Keep enum specific methods
    -keepclassmembers enum * {
        public static **[] values();
        public static ** valueOf(java.lang.String);
    }
    ```

**5. Initializing QwenClient:**

   *   It's recommended to initialize `QwenClient` once and reuse it. A good place is in the `onCreate` event of your main activity in Sketchware Pro.

    ```java
    // In your main Activity's "onCreate" event (Java section) in Sketchware Pro
    // Imports for QwenClient and SharedPreferences
    // import com.example.qwen_sdk.QwenClient; // Adjust if your package name is different
    // import android.content.SharedPreferences;
    // import android.os.Bundle;
    // (Sketchware Pro usually handles imports for standard Android classes)

    // Declare qwenClient as a field in your Activity if you need to access it from other methods/events
    // private com.example.qwen_sdk.QwenClient qwenClient;
    // private String apiKey = "YOUR_SK_API_KEY"; // Replace or load
    // private String apiCookie = "YOUR_QWEN_COOKIE"; // Replace or load

    // Inside the onCreate logic:
    // --- QwenClient Initialization ---
    // Load API key and cookie (e.g., from SharedPreferences using Sketchware blocks or Java)
    // For this example, using placeholders. **Replace these with secure loading in a real app!**
    String apiKey = "YOUR_API_KEY_FROM_STORAGE_OR_INPUT";
    String apiCookie = "YOUR_COOKIE_FROM_STORAGE_OR_INPUT";

    // It's crucial to get the Activity context correctly in Sketchware Pro's Java environment.
    // Often, 'this' or 'YourActivityName.this' works if the code is directly in the Activity's event.
    android.content.Context currentContext = this; // Or YourActivityName.this

    if (apiKey == null || apiKey.startsWith("YOUR_") || apiCookie == null || apiCookie.startsWith("YOUR_")) {
        android.widget.Toast.makeText(currentContext, "API Key/Cookie not set. Qwen features disabled.", android.widget.Toast.LENGTH_LONG).show();
        // Optionally, use Sketchware blocks to show a more integrated error message or disable UI elements.
    } else {
        // Initialize QwenClient (using default base URL and timeout)
        // Ensure the package name `com.example.qwen_sdk.QwenClient` matches your SDK's actual package.
        qwenClient = new com.example.qwen_sdk.QwenClient(apiKey, apiCookie);

        // You can now use 'qwenClient' in other Java code sections or More Blocks.
        // Store it in a global variable or pass it if needed, as per Sketchware Pro practices.
        android.widget.Toast.makeText(currentContext, "QwenClient Initialized!", android.widget.Toast.LENGTH_SHORT).show();
    }
    // --- End QwenClient Initialization ---
    ```

### Sketchware Pro Example 1: Basic Chat Completion (Non-Streaming)

**UI Description (Conceptual for Sketchware):**
*   An `EditText` (e.g., `userInputEditText`) for the user to type their message.
*   A `Button` (e.g., `sendButton`) to send the message.
*   A `TextView` (e.g., `responseTextView`) to display the Qwen API's response.

**Conceptual Sketchware Block Logic:**
1.  **Event:** `sendButton` onClick.
2.  **Action:**
    *   Get text from `userInputEditText` (using Sketchware blocks).
    *   If text is not empty:
        *   Call a Java method (defined in your Activity's Java section or a "More Block") like `performBasicChat(String message)`.
    *   Else:
        *   Show error: "Input cannot be empty" (using Sketchware Toast block).
3.  **Define Callbacks/UI Update Blocks in Sketchware:** Create "More Blocks" or use logic within your Java methods to update Sketchware UI elements.
    *   `updateResponseText(String responseText)`: Sets text of `responseTextView`.
    *   `showErrorToast(String errorMsg)`: Shows an error Toast.

**Java Code Snippet for Sketchware Pro (to be used in Activity's Java Editor or a More Block):**
*Ensure `qwenClient` is initialized (see Setup). The `runOnUiThread` calls are crucial as OkHttp callbacks are on a background thread.*

```java
// --- Basic Chat Completion (for Sketchware Pro) ---
// Assume 'qwenClient' is a field in your Activity, initialized in onCreate.
// Assume 'this' refers to your Activity context.

// Method to be called from Sketchware (e.g., button click event's Java section)
public void performBasicChat(String userMessageText) {
    if (qwenClient == null) {
        runOnUiThread(() -> android.widget.Toast.makeText(this, "QwenClient not initialized.", android.widget.Toast.LENGTH_SHORT).show());
        return;
    }
    if (userMessageText == null || userMessageText.trim().isEmpty()) {
        runOnUiThread(() -> android.widget.Toast.makeText(this, "Message cannot be empty.", android.widget.Toast.LENGTH_SHORT).show());
        return;
    }

    java.util.List<com.example.qwen_sdk.models.ChatMessage> messages = new java.util.ArrayList<>();
    messages.add(new com.example.qwen_sdk.models.ChatMessage(com.example.qwen_sdk.models.Role.USER, userMessageText));

    com.example.qwen_sdk.models.ChatRequest chatRequest = new com.example.qwen_sdk.models.ChatRequest();
    chatRequest.setModel(qwenClient.getDefaultModel());
    chatRequest.setMessages(messages);

    runOnUiThread(() -> android.widget.Toast.makeText(this, "Sending message...", android.widget.Toast.LENGTH_SHORT).show());

    qwenClient.getChatCompletion().createAsync(chatRequest, new com.example.qwen_sdk.callback.QwenCallback<com.example.qwen_sdk.models.ChatResponse>() {
        @Override
        public void onSuccess(com.example.qwen_sdk.models.ChatResponse response) {
            String responseText = "No response text found.";
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                com.example.qwen_sdk.models.ResponseMessage message = response.getChoices().get(0).getMessage();
                if (message != null && message.getContent() != null) {
                    responseText = message.getContent();
                }
            }
            final String finalResponseText = responseText;
            runOnUiThread(() -> {
                // Example: Update a TextView in Sketchware. Replace 'responseTextViewId' with your actual ID.
                // android.widget.TextView responseView = findViewById(getResources().getIdentifier("responseTextViewId", "id", getPackageName()));
                // if (responseView != null) responseView.setText(finalResponseText);
                android.widget.Toast.makeText(getApplicationContext(), "Received: " + finalResponseText.substring(0, Math.min(finalResponseText.length(), 50)) + "...", android.widget.Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public void onFailure(com.example.qwen_sdk.exception.QwenApiError error) {
            String errorMsg = "Error: " + error.getMessage();
            if (error.getErrorMessage() != null) {
                errorMsg += "\nDetails: " + error.getErrorMessage();
            }
            final String finalErrorMsg = errorMsg;
            runOnUiThread(() -> {
                 android.widget.Toast.makeText(getApplicationContext(), finalErrorMsg, android.widget.Toast.LENGTH_LONG).show();
            });
        }
    });
}
```

### Sketchware Pro Example 2: Streaming Chat Completion

**UI Description (Conceptual for Sketchware):**
*   `EditText` (e.g., `userInputEditText`).
*   `Button` (e.g., `sendStreamButton`).
*   `TextView` (e.g., `streamingResponseTextView`) to append incoming chunks.

**Conceptual Sketchware Block Logic:**
1.  **Event:** `sendStreamButton` onClick.
2.  **Action:**
    *   Get text from `userInputEditText`.
    *   Use Sketchware blocks to clear `streamingResponseTextView`.
    *   Call Java method `performStreamingChat(String message)`.
3.  **Define UI Update Logic in Java (within callbacks) or via More Blocks:**
    *   To append chunk: `streamingResponseTextView.append(chunkText);`
    *   To show completion: `Toast.makeText(this, "Streaming complete", ...).show();`
    *   To show error: `Toast.makeText(this, errorMsg, ...).show();`

**Java Code Snippet for Sketchware Pro:**
```java
// --- Streaming Chat Completion (for Sketchware Pro) ---
// Assume 'qwenClient' is initialized and 'this' is an Activity context.

public void performStreamingChat(String userMessageText) {
    if (qwenClient == null) {
        runOnUiThread(() -> android.widget.Toast.makeText(this, "QwenClient not initialized.", android.widget.Toast.LENGTH_SHORT).show());
        return;
    }
    if (userMessageText == null || userMessageText.trim().isEmpty()) {
        runOnUiThread(() -> android.widget.Toast.makeText(this, "Message cannot be empty.", android.widget.Toast.LENGTH_SHORT).show());
        return;
    }

    // Example: Clear TextView using Sketchware's ID mechanism if you have a TextView named 'streamingResponseTextView'
    // runOnUiThread(() -> {
    //    android.widget.TextView streamView = findViewById(getResources().getIdentifier("streamingResponseTextView", "id", getPackageName()));
    //    if(streamView != null) streamView.setText("");
    // });


    java.util.List<com.example.qwen_sdk.models.ChatMessage> messages = new java.util.ArrayList<>();
    messages.add(new com.example.qwen_sdk.models.ChatMessage(com.example.qwen_sdk.models.Role.USER, userMessageText));

    com.example.qwen_sdk.models.ChatRequest chatRequest = new com.example.qwen_sdk.models.ChatRequest();
    chatRequest.setModel(qwenClient.getDefaultModel());
    chatRequest.setMessages(messages);

    runOnUiThread(() -> android.widget.Toast.makeText(this, "Sending message for streaming...", android.widget.Toast.LENGTH_SHORT).show());

    qwenClient.getChatCompletion().createStreamAsync(chatRequest,
        new com.example.qwen_sdk.callback.QwenCallback<com.example.qwen_sdk.models.ChatResponseChunk>() {
            @Override
            public void onSuccess(com.example.qwen_sdk.models.ChatResponseChunk chunk) {
                if (chunk != null && chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
                    com.example.qwen_sdk.models.ResponseMessage delta = chunk.getChoices().get(0).getDelta();
                    if (delta != null && delta.getContent() != null) {
                        final String chunkText = delta.getContent();
                        runOnUiThread(() -> {
                            // Example: Append to 'streamingResponseTextView'
                            // android.widget.TextView streamView = findViewById(getResources().getIdentifier("streamingResponseTextView", "id", getPackageName()));
                            // if(streamView != null) streamView.append(chunkText);
                            // For testing, can show chunks as toasts
                            // android.widget.Toast.makeText(getApplicationContext(), "Chunk: " + chunkText, android.widget.Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }

            @Override
            public void onFailure(com.example.qwen_sdk.exception.QwenApiError error) {
                final String errorMsg = "Stream chunk error: " + error.getMessage();
                runOnUiThread(() -> android.widget.Toast.makeText(getApplicationContext(), errorMsg, android.widget.Toast.LENGTH_LONG).show());
            }
        },
        new com.example.qwen_sdk.callback.QwenCallback<Void>() { // Completion callback
            @Override
            public void onSuccess(Void response) {
                runOnUiThread(() -> android.widget.Toast.makeText(getApplicationContext(), "Streaming complete.", android.widget.Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onFailure(com.example.qwen_sdk.exception.QwenApiError error) {
                String errorMsg = "Streaming failed: " + error.getMessage();
                if (error.getErrorMessage() != null) {
                    errorMsg += "\nDetails: " + error.getErrorMessage();
                }
                final String finalErrorMsg = errorMsg;
                runOnUiThread(() -> android.widget.Toast.makeText(getApplicationContext(), finalErrorMsg, android.widget.Toast.LENGTH_LONG).show());
            }
        });
}
```

### Sketchware Pro Example 3: File Upload and Chat with Image

**UI Description (Conceptual for Sketchware):**
*   A `Button` (e.g., `pickFileButton`) to trigger Sketchware's File Picker.
*   An `ImageView` (e.g., `previewImageView`) (optional).
*   An `EditText` (e.g., `imageChatEditText`) for text query about the image.
*   A `Button` (e.g., `sendImageChatButton`).
*   A `TextView` (e.g., `imageChatResponseTextView`).

**Conceptual Sketchware Block Logic:**
1.  **Event:** `pickFileButton` onClick -> Trigger Sketchware File Picker component (select image types).
2.  **Event:** File Picker `onFilesPicked(List<String> filePaths)`:
    *   Store `selectedImagePath = filePaths.get(0);` (e.g., in an Activity String variable).
    *   (Optional) Use Sketchware blocks to load image into `previewImageView` using `selectedImagePath`.
3.  **Event:** `sendImageChatButton` onClick:
    *   Get text from `imageChatEditText`.
    *   Retrieve stored `selectedImagePath`.
    *   Call Java method `performImageChat(String filePath, String userQuery)`.
4.  **Define UI Update Logic in Java or More Blocks:**
    *   `updateImageChatResponse(String responseText)`
    *   `showImageChatError(String errorMsg)`
    *   `showImageUploadStatus(String statusMessage)`

**Java Code Snippet for Sketchware Pro:**
```java
// --- File Upload and Chat with Image (for Sketchware Pro) ---
// Assume 'qwenClient' is initialized and 'this' is an Activity context.
// String selectedImagePath; // This field would be set by your File Picker's onFilesPicked event.

public void performImageChat(final String imageFilePath, final String userQuery) {
    if (qwenClient == null) {
        runOnUiThread(() -> android.widget.Toast.makeText(this, "QwenClient not initialized.", android.widget.Toast.LENGTH_SHORT).show());
        return;
    }
    if (imageFilePath == null || imageFilePath.isEmpty()) {
        runOnUiThread(() -> android.widget.Toast.makeText(this, "Image file path is missing.", android.widget.Toast.LENGTH_SHORT).show());
        return;
    }
    final String query = (userQuery == null || userQuery.trim().isEmpty()) ? "Describe this image." : userQuery;

    java.io.File imageFile = new java.io.File(imageFilePath);
    if (!imageFile.exists()) {
        runOnUiThread(() -> android.widget.Toast.makeText(this, "Image file not found at: " + imageFilePath, android.widget.Toast.LENGTH_SHORT).show());
        return;
    }

    runOnUiThread(() -> android.widget.Toast.makeText(this, "Uploading image...", android.widget.Toast.LENGTH_SHORT).show());

    qwenClient.getFileUpload().uploadAsync(imageFile, new com.example.qwen_sdk.callback.QwenCallback<com.example.qwen_sdk.models.FileUploadResponse>() {
        @Override
        public void onSuccess(com.example.qwen_sdk.models.FileUploadResponse uploadResponse) {
            if (uploadResponse == null || uploadResponse.getFileUrl() == null) {
                runOnUiThread(() -> android.widget.Toast.makeText(getApplicationContext(), "File upload success, but no URL returned.", android.widget.Toast.LENGTH_SHORT).show());
                return;
            }

            runOnUiThread(() -> android.widget.Toast.makeText(getApplicationContext(), "Image uploaded. Sending chat...", android.widget.Toast.LENGTH_SHORT).show());

            java.util.List<com.example.qwen_sdk.models.Block> contentBlocks = new java.util.ArrayList<>();
            contentBlocks.add(new com.example.qwen_sdk.models.ImageBlock(uploadResponse.getFileUrl(), uploadResponse.getImageMimetype()));
            contentBlocks.add(new com.example.qwen_sdk.models.TextBlock(query));

            com.example.qwen_sdk.models.ChatMessage visionMessage = new com.example.qwen_sdk.models.ChatMessage(
                    com.example.qwen_sdk.models.Role.USER, contentBlocks);

            java.util.List<com.example.qwen_sdk.models.ChatMessage> messages = new java.util.ArrayList<>();
            messages.add(visionMessage);

            com.example.qwen_sdk.models.ChatRequest chatRequest = new com.example.qwen_sdk.models.ChatRequest();
            chatRequest.setModel("qwen-vl-plus"); // IMPORTANT: Use a vision-capable model
            chatRequest.setMessages(messages);

            qwenClient.getChatCompletion().createAsync(chatRequest, new com.example.qwen_sdk.callback.QwenCallback<com.example.qwen_sdk.models.ChatResponse>() {
                @Override
                public void onSuccess(com.example.qwen_sdk.models.ChatResponse chatResponse) {
                    String responseText = "No vision response text found.";
                    if (chatResponse != null && chatResponse.getChoices() != null && !chatResponse.getChoices().isEmpty()) {
                        com.example.qwen_sdk.models.ResponseMessage message = chatResponse.getChoices().get(0).getMessage();
                        if (message != null && message.getContent() != null) {
                            responseText = message.getContent();
                        }
                    }
                    final String finalResponseText = responseText;
                     runOnUiThread(() -> {
                        // Example: Update 'imageChatResponseTextView'
                        // android.widget.TextView visionResponseView = findViewById(getResources().getIdentifier("imageChatResponseTextView", "id", getPackageName()));
                        // if(visionResponseView != null) visionResponseView.setText(finalResponseText);
                        android.widget.Toast.makeText(getApplicationContext(), "Vision Response: " + finalResponseText.substring(0, Math.min(finalResponseText.length(),100)) + "...", android.widget.Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onFailure(com.example.qwen_sdk.exception.QwenApiError error) {
                    final String errorMsg = "Image chat failed: " + error.getMessage();
                    runOnUiThread(() -> android.widget.Toast.makeText(getApplicationContext(), errorMsg, android.widget.Toast.LENGTH_LONG).show());
                }
            });
        }

        @Override
        public void onFailure(com.example.qwen_sdk.exception.QwenApiError error) {
            final String errorMsg = "File upload failed: " + error.getMessage();
            runOnUiThread(() -> android.widget.Toast.makeText(getApplicationContext(), errorMsg, android.widget.Toast.LENGTH_LONG).show());
        }
    });
}
```

---
## API Reference

Detailed API documentation for each class and method is available in the Javadoc comments within the source code. Generate the Javadoc using your IDE or the `javadoc` command-line tool for a browsable HTML reference.

(Placeholder: Consider linking to generated Javadoc HTML if available online, e.g., via GitHub Pages.)

---
## Contributing

Contributions to this SDK are welcome! Please follow these general guidelines:
*   Fork the repository.
*   Create a new branch for your feature or bug fix (`git checkout -b feature/your-feature-name`).
*   Write clear and concise commit messages.
*   Ensure your code is well-documented with Javadoc.
*   Add unit tests for new functionality if applicable.
*   Make sure your changes pass any existing tests.
*   Open a pull request for review.

(More detailed contribution guidelines can be added here, e.g., coding style, testing requirements.)

---
## License

This Qwen Java SDK is released under the **MIT License**. See the [LICENSE](LICENSE) file for more details.
