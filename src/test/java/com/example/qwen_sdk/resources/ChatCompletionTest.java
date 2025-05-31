package com.example.qwen_sdk.resources;

import com.example.qwen_sdk.QwenClient;
import com.example.qwen_sdk.callback.QwenCallback;
import com.example.qwen_sdk.core.AuthManager;
import com.example.qwen_sdk.exception.QwenApiError;
import com.example.qwen_sdk.models.*;

import com.google.gson.Gson;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatCompletionTest {

    @Mock
    private QwenClient qwenClientMock;
    @Mock
    private AuthManager authManagerMock;
    @Mock
    private OkHttpClient okHttpClientMock;
    @Mock
    private Call callMock;
    @Mock
    private Response responseMock;
    @Mock
    private ResponseBody responseBodyMock;
    @Mock
    private EventSource.Factory eventSourceFactoryMock;
     @Mock
    private EventSource eventSourceMock;


    private Gson gson = new Gson(); // Use real Gson for parsing tests
    private ChatCompletion chatCompletion;

    @Captor
    private ArgumentCaptor<Request> requestCaptor;
    @Captor
    private ArgumentCaptor<Callback> okHttpCallbackCaptor;
    @Captor
    private ArgumentCaptor<EventSourceListener> eventSourceListenerCaptor;


    @BeforeEach
    void setUp() {
        // Standard mock setups
        when(qwenClientMock.getAuthManager()).thenReturn(authManagerMock);
        when(authManagerMock.getAuthorizationHeader()).thenReturn("Bearer testKey");
        when(authManagerMock.getCookieHeader()).thenReturn("testCookie");

        when(qwenClientMock.getHttpClient()).thenReturn(okHttpClientMock);
        when(qwenClientMock.getGson()).thenReturn(gson); // Use real Gson
        when(qwenClientMock.getBaseUrl()).thenReturn("https.test.qwen.ai");
        when(qwenClientMock.getLogger()).thenReturn(Logger.getLogger("TestLogger"));


        chatCompletion = new ChatCompletion(qwenClientMock);
    }

    // Test for buildRequestBody (private method) would typically be done via testing its public callers.
    // If direct testing is needed, it might require making it package-private or using reflection.
    // For this example, we'll assume its correctness is validated through the public methods.

    @Test
    void create_synchronous_successfulResponse_shouldReturnChatResponse() throws IOException {
        ChatRequest chatRequest = new ChatRequest("test-model", Collections.singletonList(new ChatMessage(Role.USER, "Hello")));
        String mockJsonResponse = "{ \"id\": \"resp-123\", \"model\": \"test-model\", \"choices\": [ { \"message\": { \"role\": \"assistant\", \"content\": \"Hi there!\" }, \"finish_reason\": \"stop\" } ], \"usage\": { \"prompt_tokens\": 10, \"completion_tokens\": 5, \"total_tokens\": 15 } }";

        when(okHttpClientMock.newCall(any(Request.class))).thenReturn(callMock);
        when(callMock.execute()).thenReturn(responseMock);
        when(responseMock.isSuccessful()).thenReturn(true);
        when(responseMock.body()).thenReturn(responseBodyMock);
        when(responseBodyMock.string()).thenReturn(mockJsonResponse);

        ChatResponse chatResponse = chatCompletion.create(chatRequest);

        assertNotNull(chatResponse);
        assertEquals("resp-123", chatResponse.getId());
        assertEquals("Hi there!", chatResponse.getChoices().get(0).getMessage().getContent());
        verify(okHttpClientMock).newCall(requestCaptor.capture());
        assertEquals(qwenClientMock.getBaseUrl() + "/v1/chat/completions", requestCaptor.getValue().url().toString());
    }

    @Test
    void create_synchronous_errorResponse_shouldThrowQwenApiError() throws IOException {
        ChatRequest chatRequest = new ChatRequest("test-model", Collections.singletonList(new ChatMessage(Role.USER, "Hello")));
        String errorJson = "{ \"error\": { \"message\": \"Test API error\", \"code\": \"test_error_code\" } }";

        when(okHttpClientMock.newCall(any(Request.class))).thenReturn(callMock);
        when(callMock.execute()).thenReturn(responseMock);
        when(responseMock.isSuccessful()).thenReturn(false);
        when(responseMock.code()).thenReturn(400);
        when(responseMock.body()).thenReturn(responseBodyMock);
        when(responseBodyMock.string()).thenReturn(errorJson);

        QwenApiError thrown = assertThrows(QwenApiError.class, () -> {
            chatCompletion.create(chatRequest);
        });
        assertTrue(thrown.getMessage().contains("400"));
        assertTrue(thrown.getErrorMessage().contains("Test API error"));
        assertEquals(400, thrown.getStatusCode());
    }

    @Test
    void create_synchronous_IOException_shouldThrowQwenApiError() throws IOException {
        ChatRequest chatRequest = new ChatRequest("test-model", Collections.singletonList(new ChatMessage(Role.USER, "Hello")));
        when(okHttpClientMock.newCall(any(Request.class))).thenReturn(callMock);
        when(callMock.execute()).thenThrow(new IOException("Network issue"));

        QwenApiError thrown = assertThrows(QwenApiError.class, () -> {
            chatCompletion.create(chatRequest);
        });
        assertTrue(thrown.getMessage().contains("Network issue"));
        assertTrue(thrown.getCause() instanceof IOException);
    }


    @SuppressWarnings("unchecked") // For QwenCallback mock
    @Test
    void createAsync_successfulResponse_shouldCallOnSuccess() throws IOException {
        ChatRequest chatRequest = new ChatRequest("test-model", Collections.singletonList(new ChatMessage(Role.USER, "Hello Async")));
        String mockJsonResponse = "{ \"id\": \"resp-async-123\", \"model\": \"test-model\", \"choices\": [ { \"message\": { \"role\": \"assistant\", \"content\": \"Hi Async!\" } } ] }";
        QwenCallback<ChatResponse> callbackMock = mock(QwenCallback.class);

        when(okHttpClientMock.newCall(any(Request.class))).thenReturn(callMock);
        // Simulate OkHttp behavior: enqueue captures the callback
        doAnswer(invocation -> {
            Callback okHttpCallback = invocation.getArgument(0);
            // Simulate successful response
            when(responseMock.isSuccessful()).thenReturn(true);
            when(responseMock.body()).thenReturn(responseBodyMock);
            when(responseBodyMock.string()).thenReturn(mockJsonResponse);
            okHttpCallback.onResponse(callMock, responseMock);
            return null;
        }).when(callMock).enqueue(any(Callback.class));


        chatCompletion.createAsync(chatRequest, callbackMock);

        ArgumentCaptor<ChatResponse> responseCaptor = ArgumentCaptor.forClass(ChatResponse.class);
        verify(callbackMock).onSuccess(responseCaptor.capture());
        assertEquals("resp-async-123", responseCaptor.getValue().getId());
        assertEquals("Hi Async!", responseCaptor.getValue().getChoices().get(0).getMessage().getContent());
    }

    @SuppressWarnings("unchecked")
    @Test
    void createAsync_errorResponse_shouldCallOnFailure() throws IOException {
        ChatRequest chatRequest = new ChatRequest("test-model", Collections.singletonList(new ChatMessage(Role.USER, "Hello Async Error")));
        String errorJson = "{ \"error\": { \"message\": \"Async Test API error\" } }";
        QwenCallback<ChatResponse> callbackMock = mock(QwenCallback.class);

        when(okHttpClientMock.newCall(any(Request.class))).thenReturn(callMock);
        doAnswer(invocation -> {
            Callback okHttpCallback = invocation.getArgument(0);
            when(responseMock.isSuccessful()).thenReturn(false);
            when(responseMock.code()).thenReturn(500);
            when(responseMock.body()).thenReturn(responseBodyMock);
            when(responseBodyMock.string()).thenReturn(errorJson);
            okHttpCallback.onResponse(callMock, responseMock);
            return null;
        }).when(callMock).enqueue(any(Callback.class));

        chatCompletion.createAsync(chatRequest, callbackMock);

        ArgumentCaptor<QwenApiError> errorCaptor = ArgumentCaptor.forClass(QwenApiError.class);
        verify(callbackMock).onFailure(errorCaptor.capture());
        assertTrue(errorCaptor.getValue().getMessage().contains("500"));
        assertTrue(errorCaptor.getValue().getErrorMessage().contains("Async Test API error"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void createAsync_IOException_shouldCallOnFailure() {
        ChatRequest chatRequest = new ChatRequest("test-model", Collections.singletonList(new ChatMessage(Role.USER, "Hello Async IOException")));
        QwenCallback<ChatResponse> callbackMock = mock(QwenCallback.class);
        IOException ioException = new IOException("Async network issue");

        when(okHttpClientMock.newCall(any(Request.class))).thenReturn(callMock);
        doAnswer(invocation -> {
            Callback okHttpCallback = invocation.getArgument(0);
            okHttpCallback.onFailure(callMock, ioException);
            return null;
        }).when(callMock).enqueue(any(Callback.class));

        chatCompletion.createAsync(chatRequest, callbackMock);

        ArgumentCaptor<QwenApiError> errorCaptor = ArgumentCaptor.forClass(QwenApiError.class);
        verify(callbackMock).onFailure(errorCaptor.capture());
        assertTrue(errorCaptor.getValue().getMessage().contains("Async network issue"));
        assertEquals(ioException, errorCaptor.getValue().getCause());
    }


    // Conceptual Test for createStreamAsync (OkHttp EventSource is tricky to mock directly without PowerMock for static EventSources.createFactory)
    // This test will mock the EventSource.Factory instead.
    @SuppressWarnings("unchecked")
    @Test
    void createStreamAsync_successfulEvents_shouldCallCallbacks() {
        ChatRequest chatRequest = new ChatRequest("test-model", Collections.singletonList(new ChatMessage(Role.USER, "Stream test")));
        chatRequest.setStream(true); // Important for buildRequestBody logic if it checks this
        QwenCallback<ChatResponseChunk> chunkCallbackMock = mock(QwenCallback.class);
        QwenCallback<Void> completionCallbackMock = mock(QwenCallback.class);

        // Mock the EventSources.createFactory part
        // This requires Mockito 5+ for mocking static methods via mockStatic, or PowerMock for older versions.
        // For this example, let's assume EventSources.createFactory(httpClient) is part of QwenClient and can be mocked.
        // If ChatCompletion directly calls EventSources.createFactory, it's harder.
        // Let's assume qwenClient.getSseEventSourceFactory() which returns a mockable factory.
        // For simplicity, as EventSources.createFactory is static, we'll just verify the listener setup conceptually.
        // We will directly capture the EventSourceListener passed to newEventSource.

        // This setup is for when EventSources.createFactory is called inside the method.
        // We need to ensure that our mocked OkHttpClient is used by the factory.
        // The actual call is `EventSources.createFactory(this.httpClient).newEventSource(request, listener);`
        // To mock this effectively, you'd typically use `mockStatic(EventSources.class)`.

        try (MockedStatic<EventSources> mockedEventSources = mockStatic(EventSources.class)) {
            mockedEventSources.when(() -> EventSources.createFactory(any(OkHttpClient.class)))
                              .thenReturn(eventSourceFactoryMock);
            when(eventSourceFactoryMock.newEventSource(any(Request.class), any(EventSourceListener.class)))
                              .thenReturn(eventSourceMock); // return a dummy mock EventSource

            chatCompletion.createStreamAsync(chatRequest, chunkCallbackMock, completionCallbackMock);

            // Capture the listener
            verify(eventSourceFactoryMock).newEventSource(requestCaptor.capture(), eventSourceListenerCaptor.capture());
            EventSourceListener listener = eventSourceListenerCaptor.getValue();

            // Simulate SSE events
            listener.onOpen(eventSourceMock, responseMock); // Simulate open

            String chunkData1 = "{ \"id\": \"chunk-1\", \"choices\": [ { \"delta\": { \"content\": \"Hello \" } } ] }";
            listener.onEvent(eventSourceMock, "event-id-1", "message", chunkData1);
            ArgumentCaptor<ChatResponseChunk> chunkCaptor1 = ArgumentCaptor.forClass(ChatResponseChunk.class);
            verify(chunkCallbackMock).onSuccess(chunkCaptor1.capture());
            assertEquals("Hello ", chunkCaptor1.getValue().getChoices().get(0).getDelta().getContent());

            String chunkData2 = "{ \"id\": \"chunk-2\", \"choices\": [ { \"delta\": { \"content\": \"World!\" } } ] }";
            listener.onEvent(eventSourceMock, "event-id-2", "message", chunkData2);
            ArgumentCaptor<ChatResponseChunk> chunkCaptor2 = ArgumentCaptor.forClass(ChatResponseChunk.class);
            verify(chunkCallbackMock, times(2)).onSuccess(chunkCaptor2.capture()); // times(2) for both chunks
            assertEquals("World!", chunkCaptor2.getValue().getChoices().get(0).getDelta().getContent());

            listener.onEvent(eventSourceMock, "event-id-done", "message", "[DONE]"); // Simulate DONE if it comes as an event

            listener.onClosed(eventSourceMock); // Simulate close
            verify(completionCallbackMock).onSuccess(null);
        }
    }

    @Test
    void createStream_synchronous_basicParsing() throws IOException {
        ChatRequest chatRequest = new ChatRequest("test-model", Collections.singletonList(new ChatMessage(Role.USER, "Hello Stream Sync")));
        String sseStreamData = "data: {\"id\":\"1\",\"choices\":[{\"delta\":{\"content\":\"Hello\"}}]}\n\n" +
                               "data: {\"id\":\"2\",\"choices\":[{\"delta\":{\"content\":\" World\"}}]}\n\n" +
                               "data: [DONE]\n\n";

        when(okHttpClientMock.newCall(any(Request.class))).thenReturn(callMock);
        when(callMock.execute()).thenReturn(responseMock);
        when(responseMock.isSuccessful()).thenReturn(true);
        when(responseMock.body()).thenReturn(responseBodyMock);
        // Use charStream() for SseIterator
        when(responseBodyMock.charStream()).thenReturn(new StringReader(sseStreamData));

        Iterable<ChatResponseChunk> iterable = chatCompletion.createStream(chatRequest);
        java.util.Iterator<ChatResponseChunk> iterator = iterable.iterator();

        assertTrue(iterator.hasNext());
        ChatResponseChunk chunk1 = iterator.next();
        assertEquals("Hello", chunk1.getChoices().get(0).getDelta().getContent());

        assertTrue(iterator.hasNext());
        ChatResponseChunk chunk2 = iterator.next();
        assertEquals(" World", chunk2.getChoices().get(0).getDelta().getContent());

        assertFalse(iterator.hasNext()); // [DONE] should terminate it
        verify(responseMock).close(); // SseIterator should close the response
    }

}
