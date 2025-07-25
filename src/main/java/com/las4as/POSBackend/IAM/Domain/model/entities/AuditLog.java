package com.las4as.POSBackend.IAM.Domain.model.entities;

import com.las4as.POSBackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor
public class AuditLog extends AuditableModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "username", nullable = false, length = 50)
    private String username;
    
    @Column(name = "action", nullable = false, length = 100)
    private String action;
    
    @Column(name = "entity_type", length = 50)
    private String entityType;
    
    @Column(name = "entity_id")
    private Long entityId;
    
    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;
    
    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "action_timestamp", nullable = false)
    private LocalDateTime actionTimestamp;
    
    @Column(name = "severity", length = 20)
    @Enumerated(EnumType.STRING)
    private AuditSeverity severity;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    public AuditLog(Long userId, String username, String action, String entityType, Long entityId,
                   String oldValues, String newValues, String ipAddress, String userAgent,
                   AuditSeverity severity, String description) {
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.actionTimestamp = LocalDateTime.now();
        this.severity = severity;
        this.description = description;
    }
    
    public enum AuditSeverity {
        LOW,     // Consultas normales
        MEDIUM,  // Modificaciones de datos
        HIGH,    // Operaciones críticas
        CRITICAL // Violaciones de seguridad
    }
}
