package com.las4as.POSBackend.IAM.Domain.model.entities;

import com.las4as.POSBackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "data_consent")
@Getter
@NoArgsConstructor
public class DataConsent extends AuditableModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "customer_document", nullable = false, length = 20)
    private String customerDocument;
    
    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;
    
    @Column(name = "consent_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ConsentType consentType;
    
    @Column(name = "purpose", nullable = false, columnDefinition = "TEXT")
    private String purpose;
    
    @Column(name = "is_granted", nullable = false)
    private boolean isGranted;
    
    @Column(name = "granted_at")
    private LocalDateTime grantedAt;
    
    @Column(name = "granted_by_user_id")
    private Long grantedByUserId;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "is_revoked", nullable = false)
    private boolean isRevoked = false;
    
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;
    
    @Column(name = "revoked_reason", length = 500)
    private String revokedReason;
    
    @Column(name = "legal_basis", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private LegalBasis legalBasis;
    
    @Column(name = "retention_period_months")
    private Integer retentionPeriodMonths;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    public DataConsent(Long customerId, String customerDocument, String customerName,
                      ConsentType consentType, String purpose, LegalBasis legalBasis,
                      Long grantedByUserId, String ipAddress, Integer retentionPeriodMonths) {
        this.customerId = customerId;
        this.customerDocument = customerDocument;
        this.customerName = customerName;
        this.consentType = consentType;
        this.purpose = purpose;
        this.legalBasis = legalBasis;
        this.grantedByUserId = grantedByUserId;
        this.ipAddress = ipAddress;
        this.retentionPeriodMonths = retentionPeriodMonths;
        this.isGranted = true;
        this.grantedAt = LocalDateTime.now();
    }
    
    public void revokeConsent(String reason) {
        this.isRevoked = true;
        this.revokedAt = LocalDateTime.now();
        this.revokedReason = reason;
    }
    
    public boolean isActive() {
        return isGranted && !isRevoked;
    }
    
    public enum ConsentType {
        SALES_PROCESSING,           // Procesamiento para ventas
        MARKETING_COMMUNICATIONS,   // Comunicaciones de marketing
        DATA_ANALYTICS,            // Análisis de datos
        LEGAL_COMPLIANCE,          // Cumplimiento legal
        CUSTOMER_SERVICE           // Atención al cliente
    }
    
    public enum LegalBasis {
        CONSENT,                   // Consentimiento (Art. 5° Ley 29733)
        CONTRACT,                  // Ejecución de contrato
        LEGAL_OBLIGATION,          // Obligación legal
        VITAL_INTERESTS,           // Intereses vitales
        PUBLIC_TASK,               // Tarea de interés público
        LEGITIMATE_INTERESTS       // Intereses legítimos
    }
}
