package com.example.message_bridge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.message_bridge.model.MessageType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** None of these tests call Twilio: each one fails before a request is made. */
class MessageServiceTest {

    private final MessageService service = new MessageService("ACtest", "SKtest", "secret", "+15005550006");

    @ParameterizedTest
    @EnumSource(value = MessageType.class, names = {"WHATSAPP", "EMAIL"})
    void rejectsChannelsNotSupportedYet(MessageType type) {
        assertThatThrownBy(() -> service.send(type, "+14155550100", "hello"))
                .isInstanceOfSatisfying(ResponseStatusException.class, e ->
                        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_IMPLEMENTED));
    }

    @ParameterizedTest
    @ValueSource(strings = {"14155550100", "+0123456789", "+1 415 555 0100", "+1415555010012345", "abc"})
    void rejectsSmsNumbersNotInE164Format(String to) {
        assertThatThrownBy(() -> service.send(MessageType.SMS, to, "hello"))
                .isInstanceOfSatisfying(ResponseStatusException.class, e ->
                        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void refusesToStartWithMissingCredentials() {
        assertThatThrownBy(() -> new MessageService("", "SKtest", "secret", "+15005550006"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("TWILIO_ACCOUNT_SID is not set");
    }
}
