package com.example.message_bridge.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private final String fromNumber;

    public SmsService(@Value("${twilio.account-sid}") String accountSid,
                      @Value("${twilio.api-key}") String apiKey,
                      @Value("${twilio.api-secret}") String apiSecret,
                      @Value("${twilio.from-number}") String fromNumber) {
        // Twilio docs: Twilio.init(API_KEY, API_SECRET, ACCOUNT_SID)
        Twilio.init(apiKey, apiSecret, accountSid);
        this.fromNumber = fromNumber;
    }

    public String send(String to, String text) {
        Message message = Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(fromNumber),
                text
        ).create();
        return message.getSid();
    }
}
