package com.eip.customer.service;

import com.eip.customer.domain.*;
import com.eip.customer.dto.*;
import com.eip.customer.exception.CustomerNotFoundException;
import com.eip.customer.exception.DuplicateCustomerException;
import com.eip.customer.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private static final AtomicLong SEQUENCE = new AtomicLong(1000);

    @Transactional
    public Customer registerCustomer(CustomerRegistrationRequest request) {
        // Check for duplicate email
        customerRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new DuplicateCustomerException("Customer with email already exists: " + request.email());
        });

        String customerId = generateCustomerId();
        String ssnHash = passwordEncoder.encode(request.ssn());

        Customer customer = Customer.builder()
                .customerId(customerId)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .dateOfBirth(request.dateOfBirth())
                .ssnHash(ssnHash)
                .status(CustomerStatus.PENDING_KYC)
                .kycStatus(KycStatus.PENDING)
                .ofacScreeningStatus(OfacStatus.PENDING)
                .consentGiven(request.consentGiven())
                .consentTimestamp(request.consentGiven() ? Instant.now() : null)
                .address(request.address() != null ? Address.builder()
                        .street1(request.address().street1())
                        .street2(request.address().street2())
                        .city(request.address().city())
                        .state(request.address().state())
                        .zipCode(request.address().zipCode())
                        .country(request.address().country() != null ? request.address().country() : "US")
                        .build() : null)
                .build();

        Customer saved = customerRepository.save(customer);

        // Publish event via outbox pattern
        outboxService.saveEvent("Customer", customerId, "customer.customers.registered",
                Map.of(
                        "customerId", customerId,
                        "firstName", request.firstName(),
                        "lastName", request.lastName(),
                        "email", request.email(),
                        "registeredAt", Instant.now().toString()
                ));

        log.info("Customer registered: {}", customerId);
        return saved;
    }

    @Transactional
    public Customer processKycCallback(KycCallbackRequest request) {
        Customer customer = getByCustomerId(request.customerId());

        customer.setKycStatus(request.status());
        customer.setKycReference(request.referenceId());
        customer.setKycCompletedAt(request.completedAt());

        if (request.status() == KycStatus.PASSED) {
            customer.setStatus(CustomerStatus.ACTIVE);
            customer.setOfacScreeningStatus(OfacStatus.CLEAR);

            outboxService.saveEvent("Customer", customer.getCustomerId(), "customer.customers.kyc-completed",
                    Map.of(
                            "customerId", customer.getCustomerId(),
                            "kycStatus", "PASSED",
                            "completedAt", request.completedAt().toString()
                    ));
        } else {
            outboxService.saveEvent("Customer", customer.getCustomerId(), "customer.customers.kyc-failed",
                    Map.of(
                            "customerId", customer.getCustomerId(),
                            "kycStatus", "FAILED",
                            "failureReason", request.failureReason() != null ? request.failureReason() : "Unknown"
                    ));
        }

        Customer updated = customerRepository.save(customer);
        log.info("KYC callback processed for customer {}: {}", customer.getCustomerId(), request.status());
        return updated;
    }

    @Transactional
    public Customer updateProfile(String customerId, ProfileUpdateRequest request) {
        Customer customer = getByCustomerId(customerId);

        if (request.phone() != null) {
            customer.setPhone(request.phone());
        }
        if (request.address() != null) {
            customer.setAddress(Address.builder()
                    .street1(request.address().street1())
                    .street2(request.address().street2())
                    .city(request.address().city())
                    .state(request.address().state())
                    .zipCode(request.address().zipCode())
                    .country(request.address().country() != null ? request.address().country() : "US")
                    .build());
        }

        Customer updated = customerRepository.save(customer);
        outboxService.saveEvent("Customer", customerId, "customer.customers.profile-updated",
                Map.of("customerId", customerId, "updatedAt", Instant.now().toString()));

        return updated;
    }

    @Transactional
    public void suspendCustomer(String customerId, String reason) {
        Customer customer = getByCustomerId(customerId);
        customer.setStatus(CustomerStatus.SUSPENDED);
        customerRepository.save(customer);

        outboxService.saveEvent("Customer", customerId, "customer.customers.suspended",
                Map.of("customerId", customerId, "reason", reason));

        log.warn("Customer suspended: {} - reason: {}", customerId, reason);
    }

    @Transactional
    public void requestGdprErasure(String customerId) {
        Customer customer = getByCustomerId(customerId);
        customer.setGdprErasureRequestedAt(Instant.now());

        // Anonymize PII
        customer.setFirstName("ANONYMIZED");
        customer.setLastName("ANONYMIZED");
        customer.setEmail("anonymized-" + customerId + "@deleted.invalid");
        customer.setPhone(null);
        customer.setSsnHash("ERASED");
        customer.setDeletedAt(Instant.now());
        customer.setStatus(CustomerStatus.CLOSED);

        customerRepository.save(customer);
        log.info("GDPR erasure processed for customer: {}", customerId);
    }

    @Transactional(readOnly = true)
    public Customer getCustomer(String customerId) {
        return getByCustomerId(customerId);
    }

    private Customer getByCustomerId(String customerId) {
        return customerRepository.findByCustomerIdAndDeletedAtIsNull(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId));
    }

    private String generateCustomerId() {
        int year = Year.now().getValue();
        long seq = SEQUENCE.getAndIncrement();
        return String.format("CUST-%d-%05d", year, seq);
    }
}
