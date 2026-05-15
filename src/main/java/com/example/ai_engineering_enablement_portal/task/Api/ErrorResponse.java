package com.example.ai_engineering_enablement_portal.task.Api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public record ErrorResponse(
        String status,
        String message,
        @JsonProperty("error_code") String errorCode,
        Map<String, Object> details) {

    public static ErrorResponse failure(String message, String errorCode) {
        return new ErrorResponse("failure", message, errorCode, null);
    }

    public static ErrorResponse failure(String message, String errorCode, Map<String, Object> details) {
        return new ErrorResponse("failure", message, errorCode, details);
    }
}
