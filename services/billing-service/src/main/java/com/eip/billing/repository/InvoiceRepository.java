package com.eip.billing.repository;

import com.eip.billing.domain.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Page<Invoice> findByPolicyId(String policyId, Pageable pageable);
    Optional<Invoice> findByInvoiceId(String invoiceId);
    List<Invoice> findByStatusAndDueDateBefore(String status, LocalDate date);
}
