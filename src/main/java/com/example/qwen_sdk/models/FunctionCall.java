package com.example.qwen_sdk.models;

// import com.google.gson.annotations.SerializedName; // If field names differ

/**
 * Represents a function call requested by the model as part of a chat response.
 * This is a placeholder structure; actual Qwen API might have more specific fields or formats.
 */
public class FunctionCall {
    private String name;
    private String arguments; // Typically a JSON string representing the arguments

    /**
     * Default constructor, primarily for use by Gson during deserialization.
     */
    public FunctionCall() {}

    /**
     * Constructs a new FunctionCall with the specified name and arguments.
     *
     * @param name The name of the function to be called.
     * @param arguments A string, typically JSON formatted, representing the arguments for the function.
     */
    public FunctionCall(String name, String arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    /**
     * Gets the name of the function to be called.
     * @return The function name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the function to be called.
     * @param name The function name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the arguments for the function call.
     * These are often represented as a JSON string.
     * @return The function arguments string.
     */
    public String getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments for the function call.
     * @param arguments A string, typically JSON formatted, representing the arguments.
     */
    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    // Considerations for future enhancements:
    // - If arguments are always JSON objects, a method to parse them into a Map<String, Object>
    //   using Gson could be added for convenience.
    // - Specific fields if the Qwen API defines a more structured format for arguments.
}
