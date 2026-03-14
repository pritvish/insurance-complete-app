package com.eip.policy.controller;

import com.eip.policy.domain.PolicyProjection;
import com.eip.policy.dto.*;
import com.eip.policy.service.PolicyCommandService;
import com.eip.policy.service.PolicyEventStore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class PolicyController {

    private final PolicyCommandService policyCommandService;
    private final PolicyEventStore eventStore;

    @PostMapping("/quotes")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BROKER', 'ADMIN')")
    public ResponseEntity<QuoteResponse> createQuote(
            @Valid @RequestBody QuoteRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(policyCommandService.createQuote(request, correlationId));
    }

    @PostMapping("/policies")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BROKER', 'ADMIN')")
    public ResponseEntity<PolicyProjection> bindPolicy(
            @Valid @RequestBody BindRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(policyCommandService.bindPolicy(request, correlationId));
    }

    @GetMapping("/policies/{policyId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BROKER', 'ADMIN', 'CLAIMS_ADJUSTER')")
    public ResponseEntity<PolicyProjection> getPolicy(@PathVariable String policyId) {
        return ResponseEntity.ok(policyCommandService.getPolicy(policyId));
    }

    @DeleteMapping("/policies/{policyId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'UNDERWRITER')")
    public ResponseEntity<PolicyProjection> cancelPolicy(
            @PathVariable String policyId,
            @RequestParam String reason,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        return ResponseEntity.ok(policyCommandService.cancelPolicy(policyId, reason, correlationId));
    }

    @GetMapping("/policies/{policyId}/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPolicyEvents(@PathVariable String policyId) {
        return ResponseEntity.ok(eventStore.getEvents(policyId));
    }
}
