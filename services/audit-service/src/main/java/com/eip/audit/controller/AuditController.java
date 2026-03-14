package com.eip.audit.controller;

import com.eip.audit.domain.AuditRecord;
import com.eip.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('COMPLIANCE', 'ADMIN')")
    public ResponseEntity<Page<AuditRecord>> getAuditHistory(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditService.getAuditHistory(entityType, entityId, pageable));
    }

    @GetMapping("/correlation/{correlationId}")
    @PreAuthorize("hasAnyRole('COMPLIANCE', 'ADMIN', 'SUPPORT')")
    public ResponseEntity<List<AuditRecord>> getByCorrelationId(
            @PathVariable String correlationId) {
        return ResponseEntity.ok(auditService.getByCorrelationId(correlationId));
    }
}
