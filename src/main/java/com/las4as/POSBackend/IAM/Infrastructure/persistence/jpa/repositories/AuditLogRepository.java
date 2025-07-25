package com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories;

import com.las4as.POSBackend.IAM.Domain.model.entities.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUserIdOrderByActionTimestampDesc(Long userId);
    
    List<AuditLog> findByActionOrderByActionTimestampDesc(String action);
    
    List<AuditLog> findByEntityTypeAndEntityIdOrderByActionTimestampDesc(String entityType, Long entityId);
    
    Page<AuditLog> findByActionTimestampBetweenOrderByActionTimestampDesc(
        LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    List<AuditLog> findBySeverityOrderByActionTimestampDesc(AuditLog.AuditSeverity severity);
    
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.actionTimestamp >= :since")
    List<AuditLog> findUserActionsAfter(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM AuditLog a WHERE a.severity = 'CRITICAL' AND a.actionTimestamp >= :since")
    List<AuditLog> findCriticalEventsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM AuditLog a WHERE a.action LIKE 'LOGIN%' AND a.userId = :userId ORDER BY a.actionTimestamp DESC")
    List<AuditLog> findLoginAttempts(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = 'LOGIN_FAILED' AND a.userId = :userId AND a.actionTimestamp >= :since")
    Long countFailedLoginAttemptsSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress AND a.action = 'LOGIN_FAILED' AND a.actionTimestamp >= :since")
    List<AuditLog> findFailedLoginsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM AuditLog a WHERE a.actionTimestamp >= :startDate AND a.actionTimestamp <= :endDate ORDER BY a.actionTimestamp DESC")
    List<AuditLog> findByActionTimestampBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
