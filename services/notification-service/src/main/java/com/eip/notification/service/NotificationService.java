package com.eip.notification.service;

import com.eip.notification.domain.*;
import com.eip.notification.repository.NotificationLogRepository;
import com.eip.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailService emailService;
    private final SmsService smsService;

    private static final AtomicLong NOTIF_SEQ = new AtomicLong(1000);

    @Transactional
    public void sendNotification(String customerId, String correlationId, String triggerEvent,
                                  String subject, String emailBody, String smsBody) {
        NotificationPreference prefs = preferenceRepository.findByCustomerId(customerId)
                .orElse(null);

        if (prefs == null) {
            log.warn("No notification preference found for customer {}, skipping", customerId);
            return;
        }

        if (prefs.isEmailEnabled() && prefs.getEmailAddress() != null) {
            sendAndLog(customerId, correlationId, triggerEvent,
                    NotificationChannel.EMAIL, prefs.getEmailAddress(), subject, emailBody);
        }

        if (prefs.isSmsEnabled() && prefs.getPhoneNumber() != null) {
            sendAndLog(customerId, correlationId, triggerEvent,
                    NotificationChannel.SMS, prefs.getPhoneNumber(), subject, smsBody);
        }
    }

    private void sendAndLog(String customerId, String correlationId, String triggerEvent,
                             NotificationChannel channel, String recipient,
                             String subject, String body) {
        String notifId = "NOTIF-" + System.currentTimeMillis() + "-" + NOTIF_SEQ.getAndIncrement();
        String providerId = null;
        NotificationStatus status;

        try {
            if (channel == NotificationChannel.EMAIL) {
                providerId = emailService.sendEmail(recipient, subject, body);
            } else {
                providerId = smsService.sendSms(recipient, body);
            }
            status = providerId != null ? NotificationStatus.SENT : NotificationStatus.FAILED;
        } catch (Exception e) {
            log.error("Notification send failed for {} channel={}: {}", customerId, channel, e.getMessage());
            status = NotificationStatus.FAILED;
        }

        NotificationLog log = NotificationLog.builder()
                .notificationId(notifId)
                .customerId(customerId)
                .correlationId(correlationId)
                .triggerEvent(triggerEvent)
                .channel(channel)
                .status(status)
                .recipient(recipient)
                .subject(subject)
                .providerMessageId(providerId)
                .sentAt(Instant.now())
                .build();

        notificationLogRepository.save(log);
    }

    @Transactional
    public void upsertPreferences(String customerId, String email, String phone,
                                   boolean emailEnabled, boolean smsEnabled) {
        NotificationPreference pref = preferenceRepository.findByCustomerId(customerId)
                .orElse(NotificationPreference.builder().customerId(customerId).build());
        pref.setEmailAddress(email);
        pref.setPhoneNumber(phone);
        pref.setEmailEnabled(emailEnabled);
        pref.setSmsEnabled(smsEnabled);
        preferenceRepository.save(pref);
    }
}
