package com.eip.notification.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final SendGrid sendGrid;

    @Value("${notification.email.from-address}")
    private String fromAddress;

    @Value("${notification.email.from-name}")
    private String fromName;

    /**
     * Sends an email via SendGrid. Returns the provider message ID or null on failure.
     */
    public String sendEmail(String toAddress, String subject, String htmlContent) {
        Email from = new Email(fromAddress, fromName);
        Email to = new Email(toAddress);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                String messageId = response.getHeaders().get("X-Message-Id");
                log.info("Email sent to {} subject='{}' messageId={}", toAddress, subject, messageId);
                return messageId != null ? messageId : "sg-" + System.currentTimeMillis();
            } else {
                log.error("SendGrid returned {} for recipient {}: {}", response.getStatusCode(), toAddress, response.getBody());
                return null;
            }
        } catch (IOException e) {
            log.error("Failed to send email to {}: {}", toAddress, e.getMessage(), e);
            return null;
        }
    }
}
