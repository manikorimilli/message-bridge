package com.example.message_bridge.controller;

import com.example.message_bridge.service.SmsService;
import com.twilio.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class MessageController {

    @Autowired
    SmsService smsService;



    public record MessageRequest(MessageType type, String to, String message) {
    }

    @PostMapping("/message")
    public ResponseEntity<String> send(@RequestBody MessageRequest request) {
        if (request.type() == null) {
            return ResponseEntity.badRequest().body("type is required (SMS, WHATSAPP or EMAIL)");
        }
        if (request.to() == null || request.to().isBlank()) {
            return ResponseEntity.badRequest().body("to is required");
        }
        if (request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().body("message is required");
        }

        return switch (request.type()) {
            case SMS -> sendSms(request);
            case WHATSAPP, EMAIL -> ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body(request.type() + " is not supported yet. Only SMS works for now.");
        };
    }

    private ResponseEntity<String> sendSms(MessageRequest request) {
        try {
            String sid = smsService.send(request.to(), request.message());
            return ResponseEntity.ok("Sent. Twilio SID: " + sid);
        } catch (ApiException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Twilio error: " + e.getMessage());
        }
    }
}
