package com.eip.customer.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customers",
        indexes = {
                @Index(name = "idx_customers_email", columnList = "email"),
                @Index(name = "idx_customers_customer_id", columnList = "customer_id"),
                @Index(name = "idx_customers_status", columnList = "status")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", unique = true, nullable = false, length = 50)
    private String customerId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "ssn_hash", nullable = false)
    private String ssnHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.PENDING_KYC;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 30)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.PENDING;

    @Column(name = "kyc_completed_at")
    private Instant kycCompletedAt;

    @Column(name = "kyc_reference")
    private String kycReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "ofac_screening_status", nullable = false, length = 30)
    @Builder.Default
    private OfacStatus ofacScreeningStatus = OfacStatus.PENDING;

    @Column(name = "credit_score")
    private Integer creditScore;

    @Column(name = "consent_given", nullable = false)
    @Builder.Default
    private Boolean consentGiven = false;

    @Column(name = "consent_timestamp")
    private Instant consentTimestamp;

    @Column(name = "gdpr_erasure_requested_at")
    private Instant gdprErasureRequestedAt;

    @Embedded
    private Address address;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    private Long version;
}
