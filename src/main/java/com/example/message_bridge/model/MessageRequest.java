package com.example.message_bridge.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageRequest(
        @NotNull MessageType type,
        @NotBlank String to,
        @NotBlank @Size(max = 1600) String message) {
}
