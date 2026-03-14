package com.eip.referencedata.service;

import com.eip.referencedata.domain.InsuranceProduct;
import com.eip.referencedata.domain.StateRule;
import com.eip.referencedata.repository.InsuranceProductRepository;
import com.eip.referencedata.repository.StateRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReferenceDataService {

    private final InsuranceProductRepository productRepository;
    private final StateRuleRepository stateRuleRepository;

    @Cacheable(value = "products", key = "'all-active'")
    @Transactional(readOnly = true)
    public List<InsuranceProduct> getAllActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }

    @Cacheable(value = "products", key = "#lineOfBusiness")
    @Transactional(readOnly = true)
    public List<InsuranceProduct> getProductsByLineOfBusiness(String lineOfBusiness) {
        return productRepository.findByLineOfBusinessAndIsActiveTrue(lineOfBusiness);
    }

    @Cacheable(value = "products", key = "#productCode")
    @Transactional(readOnly = true)
    public InsuranceProduct getProduct(String productCode) {
        return productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productCode));
    }

    @Cacheable(value = "state-rules", key = "#stateCode + ':' + #lineOfBusiness")
    @Transactional(readOnly = true)
    public StateRule getStateRule(String stateCode, String lineOfBusiness) {
        return stateRuleRepository.findByStateCodeAndLineOfBusiness(stateCode, lineOfBusiness)
                .orElseThrow(() -> new RuntimeException(
                        "State rule not found for state=" + stateCode + " lob=" + lineOfBusiness));
    }

    @Cacheable(value = "state-rules", key = "#stateCode")
    @Transactional(readOnly = true)
    public List<StateRule> getStateRules(String stateCode) {
        return stateRuleRepository.findByStateCode(stateCode);
    }
}
