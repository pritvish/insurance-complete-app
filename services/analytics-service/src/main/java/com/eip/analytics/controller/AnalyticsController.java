package com.eip.analytics.controller;

import com.eip.analytics.domain.BrokerMetrics;
import com.eip.analytics.domain.ClaimMetrics;
import com.eip.analytics.domain.PolicyMetrics;
import com.eip.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ── Policy endpoints ──────────────────────────────────────────────────────

    @GetMapping("/policies")
    @PreAuthorize("hasAnyRole('ANALYTICS_VIEWER', 'ADMIN')")
    public ResponseEntity<Page<PolicyMetrics>> getPolicies(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("lastUpdatedAt").descending());
        return ResponseEntity.ok(analyticsService.getPolicySummary(status, productCode, from, to, pageable));
    }

    @GetMapping("/policies/{policyId}")
    @PreAuthorize("hasAnyRole('ANALYTICS_VIEWER', 'ADMIN')")
    public ResponseEntity<PolicyMetrics> getPolicyMetrics(@PathVariable UUID policyId) {
        return ResponseEntity.ok(analyticsService.getPolicyMetrics(policyId));
    }

    // ── Claim endpoints ───────────────────────────────────────────────────────

    @GetMapping("/claims")
    @PreAuthorize("hasAnyRole('ANALYTICS_VIEWER', 'ADMIN')")
    public ResponseEntity<Page<ClaimMetrics>> getClaims(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("lastUpdatedAt").descending());
        return ResponseEntity.ok(analyticsService.getClaimSummary(status, from, to, pageable));
    }

    @GetMapping("/claims/{claimId}")
    @PreAuthorize("hasAnyRole('ANALYTICS_VIEWER', 'ADMIN')")
    public ResponseEntity<ClaimMetrics> getClaimMetrics(@PathVariable UUID claimId) {
        return ResponseEntity.ok(analyticsService.getClaimMetrics(claimId));
    }

    // ── Broker endpoints ──────────────────────────────────────────────────────

    @GetMapping("/brokers")
    @PreAuthorize("hasAnyRole('ANALYTICS_VIEWER', 'ADMIN')")
    public ResponseEntity<Page<BrokerMetrics>> getBrokerLeaderboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(analyticsService.getBrokerLeaderboard(pageable));
    }

    @GetMapping("/brokers/{brokerId}")
    @PreAuthorize("hasAnyRole('ANALYTICS_VIEWER', 'ADMIN')")
    public ResponseEntity<BrokerMetrics> getBrokerMetrics(@PathVariable UUID brokerId) {
        return ResponseEntity.ok(analyticsService.getBrokerMetrics(brokerId));
    }
}
