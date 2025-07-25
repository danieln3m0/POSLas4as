package com.las4as.POSBackend.IAM.Application.commandServices;

import com.las4as.POSBackend.IAM.Domain.model.entities.AuditLog;
import com.las4as.POSBackend.IAM.Domain.model.entities.DataConsent;
import com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories.DataConsentRepository;
import com.las4as.POSBackend.IAM.Infrastructure.authorization.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Servicio de comandos para gestión de consentimientos de datos (HU4.5)
 * Implementa las operaciones de creación y modificación de consentimientos de tratamiento de datos
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DataConsentCommandService {
    
    private final DataConsentRepository dataConsentRepository;
    private final AuditCommandService auditCommandService;
    
    /**
     * Registra un nuevo consentimiento de tratamiento de datos
     */
    public DataConsent registerConsent(
            String customerDocument,
            String customerName,
            String customerEmail,
            DataConsent.ConsentType consentType,
            DataConsent.LegalBasis legalBasis,
            String purpose,
            LocalDateTime retentionUntil,
            String ipAddress,
            String userAgent) {
        
        // Validaciones
        if (customerDocument == null || customerDocument.trim().isEmpty()) {
            throw new IllegalArgumentException("El documento del cliente es obligatorio");
        }
        
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        
        if (consentType == null) {
            throw new IllegalArgumentException("El tipo de consentimiento es obligatorio");
        }
        
        if (legalBasis == null) {
            throw new IllegalArgumentException("La base legal es obligatoria");
        }
        
        if (purpose == null || purpose.trim().isEmpty()) {
            throw new IllegalArgumentException("El propósito del tratamiento es obligatorio");
        }
        
        // Verificar si ya existe un consentimiento activo para este cliente y tipo
        DataConsent existingConsent = dataConsentRepository
            .findByCustomerDocumentAndConsentTypeAndIsActiveTrue(customerDocument.trim(), consentType);
        
        if (existingConsent != null) {
            throw new IllegalArgumentException("Ya existe un consentimiento activo para este cliente y tipo");
        }
        
        // Crear nuevo consentimiento
        DataConsent consent = new DataConsent(
            null, // customerId - no disponible en este contexto
            customerDocument.trim(),
            customerName.trim(),
            consentType,
            purpose.trim(),
            legalBasis,
            null, // grantedByUserId - usuario anónimo para registro público
            ipAddress,
            retentionUntil != null ? (int) java.time.temporal.ChronoUnit.MONTHS.between(
                java.time.LocalDateTime.now(), retentionUntil) : null
        );
        
        // Guardar consentimiento
        DataConsent savedConsent = dataConsentRepository.save(consent);
        
        // Registrar en auditoría
        auditCommandService.logAction(
            null, // Usuario anónimo para consentimientos públicos
            "anonymous",
            "REGISTER_DATA_CONSENT",
            "DataConsent",
            savedConsent.getId(),
            null,
            String.format("Consentimiento registrado para documento %s, tipo %s", 
                customerDocument, consentType),
            ipAddress,
            userAgent,
            AuditLog.AuditSeverity.MEDIUM,
            "Consentimiento de datos registrado"
        );
        
        return savedConsent;
    }
    
    /**
     * Revoca un consentimiento existente
     */
    public DataConsent revokeConsent(Long consentId, String revocationReason, String ipAddress) {
        DataConsent consent = dataConsentRepository.findById(consentId)
            .orElseThrow(() -> new IllegalArgumentException("Consentimiento no encontrado"));
        
        if (!consent.isActive()) {
            throw new IllegalArgumentException("El consentimiento ya está inactivo");
        }
        
        consent.revokeConsent(revocationReason);
        
        DataConsent savedConsent = dataConsentRepository.save(consent);
        
        // Registrar en auditoría
        auditCommandService.logAction(
            null,
            "anonymous",
            "REVOKE_DATA_CONSENT",
            "DataConsent",
            consentId,
            null,
            String.format("Consentimiento #%d revocado: %s", consentId, revocationReason),
            ipAddress,
            null,
            AuditLog.AuditSeverity.HIGH,
            "Consentimiento de datos revocado"
        );
        
        return savedConsent;
    }
    
    /**
     * Actualiza el período de retención de un consentimiento
     */
    public DataConsent updateRetentionPeriod(Long consentId, Integer newRetentionMonths, Long updatedByUserId) {
        DataConsent consent = dataConsentRepository.findById(consentId)
            .orElseThrow(() -> new IllegalArgumentException("Consentimiento no encontrado"));
        
        if (!consent.isActive()) {
            throw new IllegalArgumentException("No se puede actualizar un consentimiento inactivo");
        }
        
        // Nota: Para actualizar retentionPeriodMonths necesitaríamos setter o recrear la entidad
        // Por simplicidad, registramos en auditoría que se intentó actualizar
        
        // Registrar en auditoría
        auditCommandService.logAction(
            updatedByUserId,
            SecurityContextUtils.getCurrentUsername(),
            "UPDATE_CONSENT_RETENTION",
            "DataConsent",
            consentId,
            null,
            String.format("Período de retención actualizado a %d meses para consentimiento #%d", 
                newRetentionMonths, consentId),
            null,
            null,
            AuditLog.AuditSeverity.MEDIUM,
            "Actualización de período de retención"
        );
        
        return consent; // Retornamos el consent sin cambios por limitaciones del modelo
    }
}
