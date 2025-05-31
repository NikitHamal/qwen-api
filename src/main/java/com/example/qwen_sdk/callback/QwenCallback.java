package com.example.qwen_sdk.callback;

import com.example.qwen_sdk.exception.QwenApiError;

/**
 * A generic callback interface for handling asynchronous responses from the Qwen API.
 *
 * @param <T> The type of the successful response object.
 */
public interface QwenCallback<T> {

    /**
     * Called when the API request is successful.
     *
     * @param response The response object of type T, parsed from the API's output.
     */
    void onSuccess(T response);

    /**
     * Called when the API request fails or an error occurs during processing.
     *
     * @param error A {@link QwenApiError} object containing details about the failure.
     */
    void onFailure(QwenApiError error);
}
