package com.eip.analytics.repository;

import com.eip.analytics.domain.BrokerMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BrokerMetricsRepository extends JpaRepository<BrokerMetrics, UUID> {

    Optional<BrokerMetrics> findByBrokerId(UUID brokerId);

    Page<BrokerMetrics> findAllByOrderByTotalPremiumCollectedDesc(Pageable pageable);
}
