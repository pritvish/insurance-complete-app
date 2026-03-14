package com.eip.customer.repository;

import com.eip.customer.domain.Customer;
import com.eip.customer.domain.CustomerStatus;
import com.eip.customer.domain.KycStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByCustomerId(String customerId);

    @Query("SELECT c FROM Customer c WHERE c.deletedAt IS NULL AND c.status = :status AND c.kycStatus = :kycStatus")
    Page<Customer> findByStatusAndKycStatus(CustomerStatus status, KycStatus kycStatus, Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.deletedAt IS NULL")
    Page<Customer> findAllActive(Pageable pageable);

    Optional<Customer> findByCustomerIdAndDeletedAtIsNull(String customerId);
}
