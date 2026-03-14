package com.eip.referencedata.controller;

import com.eip.referencedata.domain.InsuranceProduct;
import com.eip.referencedata.domain.StateRule;
import com.eip.referencedata.service.ReferenceDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reference")
@RequiredArgsConstructor
public class ReferenceDataController {

    private final ReferenceDataService referenceDataService;

    @GetMapping("/products")
    public ResponseEntity<List<InsuranceProduct>> getAllProducts() {
        return ResponseEntity.ok(referenceDataService.getAllActiveProducts());
    }

    @GetMapping("/products/lob/{lineOfBusiness}")
    public ResponseEntity<List<InsuranceProduct>> getProductsByLob(
            @PathVariable String lineOfBusiness) {
        return ResponseEntity.ok(referenceDataService.getProductsByLineOfBusiness(lineOfBusiness));
    }

    @GetMapping("/products/{productCode}")
    public ResponseEntity<InsuranceProduct> getProduct(@PathVariable String productCode) {
        return ResponseEntity.ok(referenceDataService.getProduct(productCode));
    }

    @GetMapping("/state-rules/{stateCode}")
    public ResponseEntity<List<StateRule>> getStateRules(@PathVariable String stateCode) {
        return ResponseEntity.ok(referenceDataService.getStateRules(stateCode));
    }

    @GetMapping("/state-rules/{stateCode}/{lineOfBusiness}")
    public ResponseEntity<StateRule> getStateRule(
            @PathVariable String stateCode,
            @PathVariable String lineOfBusiness) {
        return ResponseEntity.ok(referenceDataService.getStateRule(stateCode, lineOfBusiness));
    }
}
