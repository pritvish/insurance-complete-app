package com.eip.referencedata.repository;

import com.eip.referencedata.domain.InsuranceProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InsuranceProductRepository extends JpaRepository<InsuranceProduct, UUID> {
    Optional<InsuranceProduct> findByProductCode(String productCode);
    List<InsuranceProduct> findByLineOfBusinessAndIsActiveTrue(String lineOfBusiness);
    List<InsuranceProduct> findByIsActiveTrue();
}
