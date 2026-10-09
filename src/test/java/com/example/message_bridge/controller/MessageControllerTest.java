package com.example.message_bridge.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.message_bridge.model.MessageRequest;
import com.example.message_bridge.model.MessageResponse;
import com.example.message_bridge.model.MessageType;
import com.example.message_bridge.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String VALID_SMS = """
            {"type": "SMS", "to": "+14155550100", "message": "hello"}
            """;

    @MockitoBean
    private MessageService messageService;

    @Test
    void sendsSms() throws Exception {
        given(messageService.send(MessageType.SMS, "+14155550100", "hello")).willReturn("SM123");

        postMessage("""
                {"type": "SMS", "to": "+14155550100", "message": "hello"}
                """)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value("SM123"))
                .andExpect(jsonPath("$.type").value("SMS"));
    }

    @Test
    void rejectsMissingFields() throws Exception {
        postMessage("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        "'message' must not be blank; 'to' must not be blank; 'type' must not be null"));
    }

    @Test
    void rejectsUnknownType() throws Exception {
        postMessage("""
                {"type": "FAX", "to": "+14155550100", "message": "hello"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        "Request body must be valid JSON, and 'type' must be one of [SMS, WHATSAPP, EMAIL]"));
    }

    @Test
    void rejectsMissingApiKey() throws Exception {
        mockMvc.perform(post("/v1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_SMS))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Missing or invalid X-API-Key header"));
        then(messageService).shouldHaveNoInteractions();
    }

    @Test
    void rejectsWrongApiKey() throws Exception {
        mockMvc.perform(post("/v1/message")
                        .header("X-API-Key", "wrong-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_SMS))
                .andExpect(status().isUnauthorized());
        then(messageService).shouldHaveNoInteractions();
    }

    @Test
    void allowsAnyCallerWhenNoApiKeyIsConfigured() {
        MessageService service = mock(MessageService.class);
        given(service.send(MessageType.SMS, "+14155550100", "hello")).willReturn("SM123");
        MessageController openController = new MessageController(service, "");

        MessageResponse response = openController.send(null,
                new MessageRequest(MessageType.SMS, "+14155550100", "hello"));

        assertThat(response.messageId()).isEqualTo("SM123");
    }

    @Test
    void allowsBrowserCallsFromOtherOrigins() throws Exception {
        mockMvc.perform(options("/v1/message")
                        .header("Origin", "https://message-bridge-ui.vercel.app")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://message-bridge-ui.vercel.app"));
    }

    @Test
    void returnsServiceErrorsAsProblemJson() throws Exception {
        given(messageService.send(any(), anyString(), anyString()))
                .willThrow(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Twilio could not send the SMS: down"));

        postMessage("""
                {"type": "SMS", "to": "+14155550100", "message": "hello"}
                """)
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.detail").value("Twilio could not send the SMS: down"));
    }

    /** Posts with the valid API key from src/test/resources/application.properties. */
    private ResultActions postMessage(String json) throws Exception {
        return mockMvc.perform(post("/v1/message")
                .header("X-API-Key", "test-api-key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }
}
