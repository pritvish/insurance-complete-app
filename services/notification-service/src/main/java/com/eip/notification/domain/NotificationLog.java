package com.eip.notification.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_logs", indexes = {
        @Index(name = "idx_notif_customer", columnList = "customer_id, sent_at DESC"),
        @Index(name = "idx_notif_correlation", columnList = "correlation_id"),
        @Index(name = "idx_notif_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "notification_id", nullable = false, unique = true)
    private String notificationId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "trigger_event")
    private String triggerEvent;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "subject")
    private String subject;

    @Column(name = "template_id")
    private String templateId;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "sent_at")
    private Instant sentAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
