package com.example.qwen_sdk.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AuthManagerTest {

    @Test
    void constructor_withValidArgs_shouldSucceed() {
        AuthManager authManager = new AuthManager("testApiKey", "testCookie");
        assertNotNull(authManager);
        assertEquals("Bearer testApiKey", authManager.getAuthorizationHeader());
        assertEquals("testCookie", authManager.getCookieHeader());
    }

    @Test
    void constructor_withNullApiKey_shouldThrowIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new AuthManager(null, "testCookie");
        });
        assertEquals("API key cannot be null or empty.", exception.getMessage());
    }

    @Test
    void constructor_withEmptyApiKey_shouldThrowIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new AuthManager("  ", "testCookie");
        });
        assertEquals("API key cannot be null or empty.", exception.getMessage());
    }

    @Test
    void constructor_withNullCookie_shouldThrowIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new AuthManager("testApiKey", null);
        });
        assertEquals("Cookie cannot be null or empty.", exception.getMessage());
    }

    @Test
    void constructor_withEmptyCookie_shouldThrowIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new AuthManager("testApiKey", "  ");
        });
        assertEquals("Cookie cannot be null or empty.", exception.getMessage());
    }

    @Test
    void getAuthorizationHeader_shouldReturnCorrectFormat() {
        AuthManager authManager = new AuthManager("myKey", "myCookie");
        assertEquals("Bearer myKey", authManager.getAuthorizationHeader());
    }

    @Test
    void getCookieHeader_shouldReturnCorrectCookie() {
        AuthManager authManager = new AuthManager("myKey", "myCookieValue");
        assertEquals("myCookieValue", authManager.getCookieHeader());
    }
}
