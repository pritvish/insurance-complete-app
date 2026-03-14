package com.eip.document.messaging;

import com.eip.document.domain.DocumentType;
import com.eip.document.service.DocumentService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentEventConsumer {

    private final DocumentService documentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "policy.policies.issued",
            groupId = "cg-document",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPolicyIssued(@Payload String message,
                                @Header(value = "correlationId", required = false) String correlationId) {
        try {
            JsonNode event = objectMapper.readTree(message);
            Map<String, String> data = new HashMap<>();
            data.put("policyId", getText(event, "policyId"));
            data.put("customerId", getText(event, "customerId"));
            data.put("insuredName", getText(event, "insuredName"));
            data.put("lineOfBusiness", getText(event, "lineOfBusiness"));
            data.put("coverageLimit", getText(event, "coverageLimit"));
            data.put("deductible", getText(event, "deductible"));
            data.put("effectiveDate", getText(event, "effectiveDate"));
            data.put("expirationDate", getText(event, "expirationDate"));
            data.put("premiumAmount", getText(event, "premiumAmount"));

            documentService.generateDocument(
                    DocumentType.POLICY_CERTIFICATE,
                    "Policy", getText(event, "policyId"),
                    getText(event, "customerId"),
                    correlationId, data);
        } catch (Exception e) {
            log.error("Failed to generate policy certificate document: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "claims.claims.approved",
            groupId = "cg-document",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onClaimApproved(@Payload String message,
                                 @Header(value = "correlationId", required = false) String correlationId) {
        try {
            JsonNode event = objectMapper.readTree(message);
            Map<String, String> data = new HashMap<>();
            data.put("claimId", getText(event, "claimId"));
            data.put("policyId", getText(event, "policyId"));
            data.put("customerId", getText(event, "customerId"));
            data.put("insuredName", getText(event, "insuredName"));
            data.put("approvedAmount", getText(event, "approvedAmount"));
            data.put("dateOfLoss", getText(event, "dateOfLoss"));

            documentService.generateDocument(
                    DocumentType.CLAIMS_SETTLEMENT_LETTER,
                    "Claim", getText(event, "claimId"),
                    getText(event, "customerId"),
                    correlationId, data);
        } catch (Exception e) {
            log.error("Failed to generate claims settlement document: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "billing.invoices.generated",
            groupId = "cg-document",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onInvoiceGenerated(@Payload String message,
                                    @Header(value = "correlationId", required = false) String correlationId) {
        try {
            JsonNode event = objectMapper.readTree(message);
            Map<String, String> data = new HashMap<>();
            data.put("invoiceId", getText(event, "invoiceId"));
            data.put("policyId", getText(event, "policyId"));
            data.put("customerId", getText(event, "customerId"));
            data.put("amount", getText(event, "amount"));
            data.put("dueDate", getText(event, "dueDate"));
            data.put("gracePeriodEndsAt", getText(event, "gracePeriodEndsAt"));

            documentService.generateDocument(
                    DocumentType.INVOICE,
                    "Invoice", getText(event, "invoiceId"),
                    getText(event, "customerId"),
                    correlationId, data);
        } catch (Exception e) {
            log.error("Failed to generate invoice document: {}", e.getMessage(), e);
        }
    }

    private String getText(JsonNode node, String field) {
        return node.has(field) ? node.path(field).asText("") : "";
    }
}
