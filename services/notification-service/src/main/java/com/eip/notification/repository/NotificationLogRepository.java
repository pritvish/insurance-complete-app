package com.eip.notification.repository;

import com.eip.notification.domain.NotificationLog;
import com.eip.notification.domain.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {
    Page<NotificationLog> findByCustomerIdOrderBySentAtDesc(String customerId, Pageable pageable);
    List<NotificationLog> findByCorrelationId(String correlationId);
    List<NotificationLog> findByStatus(NotificationStatus status);
}
