package com.eip.fraud.controller;

import com.eip.fraud.dto.FraudScoringRequest;
import com.eip.fraud.dto.FraudScoringResponse;
import com.eip.fraud.service.FraudScoringEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudScoringEngine fraudScoringEngine;

    @PostMapping("/score")
    public ResponseEntity<FraudScoringResponse> scoreSync(@RequestBody FraudScoringRequest request) {
        return ResponseEntity.ok(fraudScoringEngine.score(request));
    }
}
