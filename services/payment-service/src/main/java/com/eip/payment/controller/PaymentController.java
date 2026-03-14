package com.eip.payment.controller;

import com.eip.payment.domain.Payment;
import com.eip.payment.dto.PremiumPaymentRequest;
import com.eip.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/premium")
    public ResponseEntity<Payment> collectPremium(@Valid @RequestBody PremiumPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.collectPremium(request));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }
}
