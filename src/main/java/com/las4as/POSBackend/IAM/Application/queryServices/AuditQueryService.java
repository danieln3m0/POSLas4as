package com.las4as.POSBackend.IAM.Application.queryServices;

import com.las4as.POSBackend.IAM.Domain.model.entities.AuditLog;
import com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditQueryService {
    
    private final AuditLogRepository auditLogRepository;
    
    public Page<AuditLog> findAuditLogs(String username, String action, AuditLog.AuditSeverity severity,
                                       LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        
        // Implementación básica - se puede mejorar con Specifications para filtros complejos
        if (startDate != null && endDate != null) {
            return auditLogRepository.findByActionTimestampBetweenOrderByActionTimestampDesc(
                startDate, endDate, pageable);
        }
        
        return auditLogRepository.findAll(pageable);
    }
    
    public List<AuditLog> getUserActivity(Long userId, int limit) {
        List<AuditLog> allActivity = auditLogRepository.findByUserIdOrderByActionTimestampDesc(userId);
        return allActivity.size() > limit ? allActivity.subList(0, limit) : allActivity;
    }
    
    public List<AuditLog> getCriticalEventsSince(LocalDateTime since) {
        return auditLogRepository.findCriticalEventsSince(since);
    }
    
    public List<AuditLog> getLoginAttempts(Long userId) {
        return auditLogRepository.findLoginAttempts(userId);
    }
    
    public List<AuditLog> getSalesActivity(Long cashierId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        // Filtrar por acciones relacionadas con ventas
        return auditLogRepository.findByActionTimestampBetween(startDate, endDate)
            .stream()
            .filter(log -> log.getAction().startsWith("SALE_") || log.getAction().startsWith("PAYMENT_"))
            .filter(log -> cashierId == null || log.getUserId().equals(cashierId))
            .toList();
    }
    
    public List<AuditLog> getInventoryChanges(Long productId, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        return auditLogRepository.findByActionTimestampBetween(startDate, endDate)
            .stream()
            .filter(log -> log.getAction().startsWith("STOCK_") || log.getAction().startsWith("PRODUCT_"))
            .filter(log -> productId == null || (log.getEntityId() != null && log.getEntityId().equals(productId)))
            .toList();
    }
    
    public Long getFailedLoginAttemptsSince(Long userId, LocalDateTime since) {
        return auditLogRepository.countFailedLoginAttemptsSince(userId, since);
    }
    
    public List<AuditLog> getFailedLoginsByIpSince(String ipAddress, LocalDateTime since) {
        return auditLogRepository.findFailedLoginsByIpSince(ipAddress, since);
    }
}
