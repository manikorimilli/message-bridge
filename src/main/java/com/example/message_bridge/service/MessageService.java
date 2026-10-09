package com.example.message_bridge.service;

import com.example.message_bridge.model.MessageType;
import com.twilio.exception.TwilioException;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.server.ResponseStatusException;

/**
 * Sends a message through the channel matching its type. Stateless: nothing is stored.
 * To add a channel (WhatsApp, email), add a private send method and point its case to it.
 */
@Service
public class MessageService {

    /** E.164: a plus sign, country code and number, up to 15 digits, e.g. +14155550100. */
    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{6,14}$");

    private final TwilioRestClient twilio;
    private final PhoneNumber smsFrom;

    public MessageService(@Value("${twilio.account-sid}") String accountSid,
                          @Value("${twilio.api-key}") String apiKey,
                          @Value("${twilio.api-secret}") String apiSecret,
                          @Value("${twilio.from-number}") String fromNumber) {
        Assert.hasText(accountSid, "TWILIO_ACCOUNT_SID is not set");
        Assert.hasText(apiKey, "TWILIO_API_KEY is not set");
        Assert.hasText(apiSecret, "TWILIO_API_SECRET is not set");
        Assert.hasText(fromNumber, "TWILIO_FROM_NUMBER is not set");
        this.twilio = new TwilioRestClient.Builder(apiKey, apiSecret).accountSid(accountSid).build();
        this.smsFrom = new PhoneNumber(fromNumber);
    }

    /** Sends the message and returns the provider's message id. */
    public String send(MessageType type, String to, String message) {
        return switch (type) {
            case SMS -> sendSms(to, message);
            case WHATSAPP, EMAIL -> throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                    type + " messages are not supported yet");
        };
    }

    private String sendSms(String to, String message) {
        if (!E164.matcher(to).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "'to' must be a phone number in E.164 format, for example +14155550100");
        }
        try {
            return Message.creator(new PhoneNumber(to), smsFrom, message).create(twilio).getSid();
        } catch (TwilioException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Twilio could not send the SMS: " + e.getMessage(), e);
        }
    }
}
