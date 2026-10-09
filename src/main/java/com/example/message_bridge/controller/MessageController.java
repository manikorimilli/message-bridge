package com.example.message_bridge.controller;

import com.example.message_bridge.model.MessageRequest;
import com.example.message_bridge.model.MessageResponse;
import com.example.message_bridge.service.MessageService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;
    /** Null when API_KEY is not set: the API is then open to everyone. */
    private final byte[] apiKey;

    public MessageController(MessageService messageService, @Value("${api.key}") String apiKey) {
        this.messageService = messageService;
        if (apiKey.isBlank()) {
            this.apiKey = null;
            log.warn("API_KEY is not set: anyone who can reach this server can send messages");
        } else {
            this.apiKey = apiKey.getBytes(StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/v1/message")
    public MessageResponse send(@RequestHeader(value = "X-API-Key", required = false) String providedKey,
                                @Valid @RequestBody MessageRequest request) {
        requireValidApiKey(providedKey);
        String messageId = messageService.send(request.type(), request.to(), request.message());
        return new MessageResponse(messageId, request.type());
    }

    /** Constant-time comparison, so response timing does not leak how much of the key matched. */
    private void requireValidApiKey(String providedKey) {
        if (apiKey == null) {
            return;
        }
        if (providedKey == null
                || !MessageDigest.isEqual(apiKey, providedKey.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid X-API-Key header");
        }
    }
}
