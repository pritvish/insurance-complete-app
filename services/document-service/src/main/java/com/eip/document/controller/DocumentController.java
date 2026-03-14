package com.eip.document.controller;

import com.eip.document.domain.DocumentRecord;
import com.eip.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADJUSTER', 'UNDERWRITER', 'CUSTOMER')")
    public ResponseEntity<List<DocumentRecord>> getDocumentsForEntity(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        return ResponseEntity.ok(documentService.getDocumentsForEntity(entityType, entityId));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'CUSTOMER')")
    public ResponseEntity<List<DocumentRecord>> getDocumentsForCustomer(
            @PathVariable String customerId) {
        return ResponseEntity.ok(documentService.getDocumentsForCustomer(customerId));
    }

    @GetMapping("/{documentId}/download-url")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'CUSTOMER', 'ADJUSTER')")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable String documentId) {
        String signedUrl = documentService.getSignedDownloadUrl(documentId);
        return ResponseEntity.ok(Map.of("downloadUrl", signedUrl, "documentId", documentId));
    }

    @PostMapping("/{documentId}/sign")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<DocumentRecord> signDocument(
            @PathVariable String documentId,
            @RequestParam String signedBy) {
        return ResponseEntity.ok(documentService.markSigned(documentId, signedBy));
    }
}
