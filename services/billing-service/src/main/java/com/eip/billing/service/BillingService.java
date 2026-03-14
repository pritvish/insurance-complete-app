package com.eip.billing.service;

import com.eip.billing.domain.*;
import com.eip.billing.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final BillingAccountRepository billingAccountRepository;
    private final InvoiceRepository invoiceRepository;
    private static final AtomicLong ACCOUNT_SEQ = new AtomicLong(1000);
    private static final AtomicLong INVOICE_SEQ = new AtomicLong(1000);

    @Transactional
    public BillingAccount createBillingAccount(String policyId, String customerId,
                                                BigDecimal premiumAmount, String billingFrequency) {
        String accountId = "BILL-" + Year.now().getValue() + "-" + ACCOUNT_SEQ.getAndIncrement();

        BillingAccount account = BillingAccount.builder()
                .accountId(accountId)
                .policyId(policyId)
                .customerId(customerId)
                .billingFrequency(billingFrequency)
                .nextDueDate(LocalDate.now().plusMonths(1))
                .outstandingBalance(premiumAmount)
                .status("ACTIVE")
                .build();

        account = billingAccountRepository.save(account);

        // Generate first invoice
        generateInvoice(account, premiumAmount);
        log.info("Billing account created: {} for policy {}", accountId, policyId);
        return account;
    }

    @Transactional
    public Invoice generateInvoice(BillingAccount account, BigDecimal amount) {
        String invoiceId = "INV-" + Year.now().getValue() + "-" + INVOICE_SEQ.getAndIncrement();

        Invoice invoice = Invoice.builder()
                .invoiceId(invoiceId)
                .accountId(account.getAccountId())
                .policyId(account.getPolicyId())
                .amount(amount)
                .dueDate(account.getNextDueDate())
                .gracePeriodEndsAt(account.getNextDueDate().plusDays(30))
                .status("ISSUED")
                .build();

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public void recordPayment(String invoiceId, BigDecimal amountPaid) {
        invoiceRepository.findByInvoiceId(invoiceId).ifPresent(invoice -> {
            invoice.setStatus("PAID");
            invoice.setPaidAt(Instant.now());
            invoiceRepository.save(invoice);

            billingAccountRepository.findByPolicyId(invoice.getPolicyId()).ifPresent(account -> {
                account.setOutstandingBalance(account.getOutstandingBalance().subtract(amountPaid));
                account.setUpdatedAt(Instant.now());
                billingAccountRepository.save(account);
            });
        });
    }

    @Scheduled(cron = "0 0 8 * * *") // Daily at 8am
    @Transactional
    public void processOverdueInvoices() {
        List<Invoice> overdueInvoices = invoiceRepository
                .findByStatusAndDueDateBefore("ISSUED", LocalDate.now().minusDays(30));

        overdueInvoices.forEach(invoice -> {
            invoice.setStatus("OVERDUE");
            invoiceRepository.save(invoice);
            log.warn("Invoice {} marked OVERDUE for policy {}", invoice.getInvoiceId(), invoice.getPolicyId());
        });
    }

    @Transactional(readOnly = true)
    public BillingAccount getAccountByPolicy(String policyId) {
        return billingAccountRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new RuntimeException("Billing account not found for policy: " + policyId));
    }
}
