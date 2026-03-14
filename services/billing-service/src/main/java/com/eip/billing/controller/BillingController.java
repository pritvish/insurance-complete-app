package com.eip.billing.controller;

import com.eip.billing.domain.BillingAccount;
import com.eip.billing.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/{policyId}/account")
    public ResponseEntity<BillingAccount> getAccount(@PathVariable String policyId) {
        return ResponseEntity.ok(billingService.getAccountByPolicy(policyId));
    }
}
