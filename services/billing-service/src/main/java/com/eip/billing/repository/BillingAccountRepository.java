package com.eip.billing.repository;

import com.eip.billing.domain.BillingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface BillingAccountRepository extends JpaRepository<BillingAccount, UUID> {
    Optional<BillingAccount> findByPolicyId(String policyId);
    Optional<BillingAccount> findByAccountId(String accountId);
}
