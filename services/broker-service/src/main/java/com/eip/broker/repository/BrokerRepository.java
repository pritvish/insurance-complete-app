package com.eip.broker.repository;

import com.eip.broker.domain.Broker;
import com.eip.broker.domain.BrokerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrokerRepository extends JpaRepository<Broker, UUID> {
    Optional<Broker> findByBrokerId(String brokerId);
    Optional<Broker> findByEmail(String email);
    Optional<Broker> findByLicenseNumber(String licenseNumber);
    Page<Broker> findByStatus(BrokerStatus status, Pageable pageable);
}
