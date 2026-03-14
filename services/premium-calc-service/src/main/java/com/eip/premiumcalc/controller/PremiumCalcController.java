package com.eip.premiumcalc.controller;

import com.eip.premiumcalc.dto.PremiumCalculationRequest;
import com.eip.premiumcalc.dto.PremiumCalculationResponse;
import com.eip.premiumcalc.service.RatingEngine;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/premium")
@RequiredArgsConstructor
public class PremiumCalcController {

    private final RatingEngine ratingEngine;

    @PostMapping("/calculate")
    public ResponseEntity<PremiumCalculationResponse> calculate(
            @Valid @RequestBody PremiumCalculationRequest request) {
        return ResponseEntity.ok(ratingEngine.calculate(request));
    }

    @GetMapping("/rate-tables")
    public ResponseEntity<?> getRateTables() {
        return ResponseEntity.ok("Rate tables endpoint - use /calculate for premium calculation");
    }
}
