package com.gateway.apigateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class NotificationRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "type is required")
    @Pattern(regexp = "EMAIL|PUSH", message = "type must be EMAIL, or PUSH")
    private String type;

    @NotBlank(message = "recipient is required")
    private String recipient;

    private String subject;  // optional for SMS/Push

    @NotBlank(message = "body is required")
    private String body;

    private Map<String, String> metadata;

}
