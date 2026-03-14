package com.eip.customer.controller;

import com.eip.customer.domain.Customer;
import com.eip.customer.dto.*;
import com.eip.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<Customer> registerCustomer(
            @Valid @RequestBody CustomerRegistrationRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        log.info("Registering customer, correlationId={}", correlationId);
        Customer customer = customerService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'CLAIMS_ADJUSTER')")
    public ResponseEntity<Customer> getCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(customerService.getCustomer(customerId));
    }

    @PutMapping("/{customerId}/profile")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<Customer> updateProfile(
            @PathVariable String customerId,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(customerService.updateProfile(customerId, request));
    }

    @PostMapping("/{customerId}/kyc/callback")
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<Customer> kycCallback(
            @PathVariable String customerId,
            @Valid @RequestBody KycCallbackRequest request) {
        return ResponseEntity.ok(customerService.processKycCallback(request));
    }

    @DeleteMapping("/{customerId}/gdpr")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'COMPLIANCE')")
    public ResponseEntity<Void> requestGdprErasure(@PathVariable String customerId) {
        customerService.requestGdprErasure(customerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{customerId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> suspendCustomer(
            @PathVariable String customerId,
            @RequestParam String reason) {
        customerService.suspendCustomer(customerId, reason);
        return ResponseEntity.noContent().build();
    }
}
