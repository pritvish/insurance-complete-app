package com.eip.broker.controller;

import com.eip.broker.domain.Broker;
import com.eip.broker.dto.BrokerPortfolioSummary;
import com.eip.broker.dto.BrokerRegistrationRequest;
import com.eip.broker.service.BrokerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/brokers")
@RequiredArgsConstructor
public class BrokerController {

    private final BrokerService brokerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
    public ResponseEntity<Broker> registerBroker(@Valid @RequestBody BrokerRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brokerService.registerBroker(request));
    }

    @GetMapping("/{brokerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER', 'ADJUSTER')")
    public ResponseEntity<Broker> getBroker(@PathVariable String brokerId) {
        return ResponseEntity.ok(brokerService.getBroker(brokerId));
    }

    @GetMapping("/{brokerId}/portfolio")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER', 'BROKER')")
    public ResponseEntity<BrokerPortfolioSummary> getPortfolio(@PathVariable String brokerId) {
        return ResponseEntity.ok(brokerService.getPortfolioSummary(brokerId));
    }

    @PostMapping("/{brokerId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Broker> suspendBroker(@PathVariable String brokerId) {
        return ResponseEntity.ok(brokerService.suspendBroker(brokerId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
    public ResponseEntity<Page<Broker>> getActiveBrokers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(brokerService.getActiveBrokers(pageable));
    }
}
