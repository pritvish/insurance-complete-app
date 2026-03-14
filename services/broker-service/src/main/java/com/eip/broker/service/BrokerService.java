package com.eip.broker.service;

import com.eip.broker.domain.Broker;
import com.eip.broker.domain.BrokerStatus;
import com.eip.broker.dto.BrokerPortfolioSummary;
import com.eip.broker.dto.BrokerRegistrationRequest;
import com.eip.broker.repository.BrokerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrokerService {

    private final BrokerRepository brokerRepository;
    private static final AtomicLong BROKER_SEQ = new AtomicLong(1000);

    @Transactional
    public Broker registerBroker(BrokerRegistrationRequest request) {
        brokerRepository.findByEmail(request.email()).ifPresent(b -> {
            throw new RuntimeException("Broker already registered with email: " + request.email());
        });

        String brokerId = "BRK-" + Year.now().getValue() + "-" + BROKER_SEQ.getAndIncrement();

        Broker broker = Broker.builder()
                .brokerId(brokerId)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .agencyName(request.agencyName())
                .licenseNumber(request.licenseNumber())
                .licenseState(request.licenseState())
                .licenseExpiryDate(request.licenseExpiryDate())
                .commissionRate(request.commissionRate() != null ? request.commissionRate() : new BigDecimal("0.0750"))
                .status(BrokerStatus.ACTIVE)
                .build();

        broker = brokerRepository.save(broker);
        log.info("Broker registered: {} name={} {} agency={}", brokerId, request.firstName(), request.lastName(), request.agencyName());
        return broker;
    }

    @Transactional(readOnly = true)
    public Broker getBroker(String brokerId) {
        return brokerRepository.findByBrokerId(brokerId)
                .orElseThrow(() -> new RuntimeException("Broker not found: " + brokerId));
    }

    @Transactional(readOnly = true)
    public BrokerPortfolioSummary getPortfolioSummary(String brokerId) {
        Broker broker = getBroker(brokerId);
        BigDecimal estimatedCommission = broker.getYtdPremiumVolume()
                .multiply(broker.getCommissionRate());

        return new BrokerPortfolioSummary(
                broker.getBrokerId(),
                broker.getFirstName() + " " + broker.getLastName(),
                broker.getAgencyName(),
                broker.getYtdPolicyCount(),
                broker.getYtdPremiumVolume(),
                broker.getCommissionRate(),
                estimatedCommission,
                broker.getStatus().name()
        );
    }

    @Transactional
    public void updatePortfolioStats(String brokerId, BigDecimal premiumAmount) {
        brokerRepository.findByBrokerId(brokerId).ifPresent(broker -> {
            broker.setYtdPremiumVolume(broker.getYtdPremiumVolume().add(premiumAmount));
            broker.setYtdPolicyCount(broker.getYtdPolicyCount() + 1);
            brokerRepository.save(broker);
        });
    }

    @Transactional
    public Broker suspendBroker(String brokerId) {
        Broker broker = getBroker(brokerId);
        broker.setStatus(BrokerStatus.SUSPENDED);
        return brokerRepository.save(broker);
    }

    @Transactional(readOnly = true)
    public Page<Broker> getActiveBrokers(Pageable pageable) {
        return brokerRepository.findByStatus(BrokerStatus.ACTIVE, pageable);
    }
}
