package com.eip.notification.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${notification.sms.from-number}")
    private String fromNumber;

    /**
     * Sends an SMS via Twilio. Returns Twilio SID or null on failure.
     * Twilio.init() must have been called in TwilioConfig before this.
     */
    public String sendSms(String toPhoneNumber, String body) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(fromNumber),
                    body
            ).create();

            log.info("SMS sent to {} sid={} status={}", toPhoneNumber, message.getSid(), message.getStatus());
            return message.getSid();
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toPhoneNumber, e.getMessage(), e);
            return null;
        }
    }
}
