package com.eip.notification.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_preferences",
       uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private String customerId;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Builder.Default
    @Column(name = "email_enabled")
    private boolean emailEnabled = true;

    @Builder.Default
    @Column(name = "sms_enabled")
    private boolean smsEnabled = true;

    @Builder.Default
    @Column(name = "marketing_opted_in")
    private boolean marketingOptedIn = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
