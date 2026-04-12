package com.gateway.apigateway.controller;

import com.common.event.NotificationEvent;
import com.gateway.apigateway.dto.NotificationRequest;
import com.gateway.apigateway.producer.NotificationProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@Slf4j
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationProducer producer;


    @PostMapping
    public ResponseEntity<Map<String, String>> send(
            @Valid @RequestBody NotificationRequest request) {

        String correlationId = UUID.randomUUID().toString();

        NotificationEvent event = new NotificationEvent();
        event.setUserId(request.getUserId());
        event.setType(request.getType());
        event.setRecipient(request.getRecipient());
        event.setSubject(request.getSubject());
        event.setBody(request.getBody());
        event.setCorrelationId(correlationId);
        event.setMetadata(request.getMetadata());

        producer.send(event);

        log.info("Notification accepted: correlationId={}, type={}",
                correlationId, request.getType());

        // WHY 202 Accepted (not 200 OK): The notification is queued,
        // not processed yet. 202 means "I received it, I'll handle it later."
        return ResponseEntity.accepted().body(Map.of(
                "status", "ACCEPTED",
                "correlationId", correlationId
        ));
    }
}
