package com.eip.notification.controller;

import com.eip.notification.domain.NotificationLog;
import com.eip.notification.repository.NotificationLogRepository;
import com.eip.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationLogRepository notificationLogRepository;

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT', 'CUSTOMER')")
    public ResponseEntity<Page<NotificationLog>> getCustomerNotifications(
            @PathVariable String customerId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                notificationLogRepository.findByCustomerIdOrderBySentAtDesc(customerId, pageable));
    }
}
