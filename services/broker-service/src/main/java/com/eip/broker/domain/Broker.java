package com.eip.broker.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "brokers", indexes = {
        @Index(name = "idx_broker_id", columnList = "broker_id"),
        @Index(name = "idx_broker_email", columnList = "email"),
        @Index(name = "idx_broker_license", columnList = "license_number")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Broker {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "broker_id", nullable = false, unique = true)
    private String brokerId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "agency_name")
    private String agencyName;

    @Column(name = "license_number", nullable = false, unique = true)
    private String licenseNumber;

    @Column(name = "license_state")
    private String licenseState;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BrokerStatus status = BrokerStatus.ACTIVE;

    @Column(name = "commission_rate", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal commissionRate = new BigDecimal("0.0750"); // 7.5% default

    @Column(name = "ytd_premium_volume", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal ytdPremiumVolume = BigDecimal.ZERO;

    @Column(name = "ytd_policy_count")
    @Builder.Default
    private Integer ytdPolicyCount = 0;

    @Version
    @Column(name = "version")
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
