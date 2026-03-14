package com.eip.customer.service;

import com.eip.customer.domain.*;
import com.eip.customer.dto.*;
import com.eip.customer.exception.CustomerNotFoundException;
import com.eip.customer.exception.DuplicateCustomerException;
import com.eip.customer.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private OutboxService outboxService;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private CustomerService customerService;

    private BCryptPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder(12);
    }

    @Test
    void registerCustomer_success() {
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "John", "Doe", "john.doe@example.com", "+15551234567",
                LocalDate.of(1985, 6, 15), "123456789",
                new AddressDto("123 Main St", null, "Springfield", "IL", "62701", "US"),
                true
        );

        when(customerRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        when(outboxService.saveEvent(anyString(), anyString(), anyString(), any())).thenReturn(null);

        Customer result = customerService.registerCustomer(request);

        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.getStatus()).isEqualTo(CustomerStatus.PENDING_KYC);
        assertThat(result.getKycStatus()).isEqualTo(KycStatus.PENDING);
        assertThat(result.getCustomerId()).startsWith("CUST-");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void registerCustomer_duplicateEmail_throwsException() {
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Jane", "Doe", "jane@example.com", null,
                LocalDate.of(1990, 1, 1), "987654321",
                null, true
        );

        when(customerRepository.findByEmail("jane@example.com"))
                .thenReturn(Optional.of(new Customer()));

        assertThatThrownBy(() -> customerService.registerCustomer(request))
                .isInstanceOf(DuplicateCustomerException.class);

        verify(customerRepository, never()).save(any());
    }

    @Test
    void processKycCallback_passed_activatesCustomer() {
        Customer customer = Customer.builder()
                .customerId("CUST-2026-00001")
                .status(CustomerStatus.PENDING_KYC)
                .kycStatus(KycStatus.PENDING)
                .build();

        KycCallbackRequest callback = new KycCallbackRequest(
                "CUST-2026-00001", KycStatus.PASSED, "KYC-REF-123",
                Instant.now(), null
        );

        when(customerRepository.findByCustomerIdAndDeletedAtIsNull("CUST-2026-00001"))
                .thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(outboxService.saveEvent(anyString(), anyString(), anyString(), any())).thenReturn(null);

        Customer result = customerService.processKycCallback(callback);

        assertThat(result.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(result.getKycStatus()).isEqualTo(KycStatus.PASSED);
    }

    @Test
    void getCustomer_notFound_throwsException() {
        when(customerRepository.findByCustomerIdAndDeletedAtIsNull("NONEXISTENT"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomer("NONEXISTENT"))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void requestGdprErasure_anonymizesPii() {
        Customer customer = Customer.builder()
                .customerId("CUST-2026-00002")
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .ssnHash("hashed-ssn")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(customerRepository.findByCustomerIdAndDeletedAtIsNull("CUST-2026-00002"))
                .thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        customerService.requestGdprErasure("CUST-2026-00002");

        assertThat(customer.getFirstName()).isEqualTo("ANONYMIZED");
        assertThat(customer.getLastName()).isEqualTo("ANONYMIZED");
        assertThat(customer.getSsnHash()).isEqualTo("ERASED");
        assertThat(customer.getStatus()).isEqualTo(CustomerStatus.CLOSED);
        assertThat(customer.getDeletedAt()).isNotNull();
    }
}
