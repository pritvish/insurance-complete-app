package com.eip.notification.config;

import com.sendgrid.SendGrid;
import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfig {

    @Value("${notification.sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${notification.twilio.account-sid}")
    private String twilioAccountSid;

    @Value("${notification.twilio.auth-token}")
    private String twilioAuthToken;

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(sendGridApiKey);
    }

    @PostConstruct
    public void initTwilio() {
        Twilio.init(twilioAccountSid, twilioAuthToken);
    }
}
