package com.eip.claims.controller;

import com.eip.claims.domain.Claim;
import com.eip.claims.dto.*;
import com.eip.claims.service.ClaimsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/claims")
@RequiredArgsConstructor
public class ClaimsController {

    private final ClaimsService claimsService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'BROKER', 'ADMIN')")
    public ResponseEntity<Claim> fileClaim(@Valid @RequestBody FnolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(claimsService.fileClaim(request));
    }

    @GetMapping("/{claimId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'CLAIMS_ADJUSTER', 'ADMIN')")
    public ResponseEntity<Claim> getClaim(@PathVariable String claimId) {
        return ResponseEntity.ok(claimsService.getClaim(claimId));
    }

    @PutMapping("/{claimId}/assign")
    @PreAuthorize("hasAnyRole('CLAIMS_MANAGER', 'ADMIN')")
    public ResponseEntity<Claim> assignAdjuster(
            @PathVariable String claimId,
            @Valid @RequestBody ClaimAssignmentRequest request) {
        return ResponseEntity.ok(claimsService.assignAdjuster(request));
    }

    @PutMapping("/{claimId}/reserve")
    @PreAuthorize("hasAnyRole('CLAIMS_ADJUSTER', 'ADMIN')")
    public ResponseEntity<Claim> setReserve(
            @PathVariable String claimId,
            @Valid @RequestBody ReserveRequest request) {
        return ResponseEntity.ok(claimsService.setReserve(request));
    }

    @PostMapping("/{claimId}/approve")
    @PreAuthorize("hasAnyRole('CLAIMS_MANAGER', 'ADMIN')")
    public ResponseEntity<Claim> approveClaim(
            @PathVariable String claimId,
            @RequestParam BigDecimal approvedAmount,
            @RequestParam String approvedBy) {
        return ResponseEntity.ok(claimsService.approveClaim(claimId, approvedAmount, approvedBy));
    }

    @PostMapping("/{claimId}/close")
    @PreAuthorize("hasAnyRole('CLAIMS_ADJUSTER', 'ADMIN')")
    public ResponseEntity<Claim> closeClaim(@PathVariable String claimId) {
        return ResponseEntity.ok(claimsService.closeClaim(claimId));
    }

    @PostMapping("/{claimId}/litigation-flag")
    @PreAuthorize("hasAnyRole('CLAIMS_MANAGER', 'ADMIN')")
    public ResponseEntity<Claim> flagForLitigation(@PathVariable String claimId) {
        return ResponseEntity.ok(claimsService.flagForLitigation(claimId));
    }
}
