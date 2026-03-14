package com.eip.analytics.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "broker_metrics",
        indexes = {
                @Index(name = "idx_bm_broker_id", columnList = "broker_id", unique = true),
                @Index(name = "idx_bm_total_premium", columnList = "total_premium_collected")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrokerMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "broker_id", nullable = false, unique = true)
    private UUID brokerId;

    @Column(name = "broker_name", length = 200)
    private String brokerName;

    @Column(name = "total_policies")
    @Builder.Default
    private Integer totalPolicies = 0;

    @Column(name = "active_policies")
    @Builder.Default
    private Integer activePolicies = 0;

    @Column(name = "total_premium_collected", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalPremiumCollected = BigDecimal.ZERO;

    @Column(name = "total_claims_count")
    @Builder.Default
    private Integer totalClaimsCount = 0;

    @Column(name = "claims_ratio", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal claimsRatio = BigDecimal.ZERO;

    @Column(name = "last_event_type", length = 100)
    private String lastEventType;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    @Version
    private Long version;
}
