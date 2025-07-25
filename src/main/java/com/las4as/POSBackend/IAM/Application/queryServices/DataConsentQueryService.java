package com.las4as.POSBackend.IAM.Application.queryServices;

import com.las4as.POSBackend.IAM.Domain.model.entities.DataConsent;
import com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories.DataConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de consultas para consentimientos de datos (HU4.5)
 * Implementa las operaciones de lectura y búsqueda de consentimientos de tratamiento de datos
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DataConsentQueryService {
    
    private final DataConsentRepository dataConsentRepository;
    
    /**
     * Busca consentimientos por documento del cliente
     */
    public List<DataConsent> findByCustomerDocument(String customerDocument) {
        if (customerDocument == null || customerDocument.trim().isEmpty()) {
            throw new IllegalArgumentException("El documento del cliente es obligatorio");
        }
        return dataConsentRepository.findByCustomerDocumentOrderByGrantedAtDesc(customerDocument.trim());
    }
    
    /**
     * Busca consentimientos activos por documento del cliente
     */
    public List<DataConsent> findActiveConsentsByCustomer(String customerDocument) {
        if (customerDocument == null || customerDocument.trim().isEmpty()) {
            throw new IllegalArgumentException("El documento del cliente es obligatorio");
        }
        return dataConsentRepository.findByCustomerDocumentAndIsActiveTrueOrderByGrantedAtDesc(customerDocument.trim());
    }
    
    /**
     * Verifica si existe un consentimiento activo para un cliente y tipo específico
     */
    public boolean hasActiveConsent(String customerDocument, DataConsent.ConsentType consentType) {
        if (customerDocument == null || customerDocument.trim().isEmpty()) {
            throw new IllegalArgumentException("El documento del cliente es obligatorio");
        }
        if (consentType == null) {
            throw new IllegalArgumentException("El tipo de consentimiento es obligatorio");
        }
        
        DataConsent consent = dataConsentRepository
            .findByCustomerDocumentAndConsentTypeAndIsActiveTrue(customerDocument.trim(), consentType);
        return consent != null;
    }
    
    /**
     * Obtiene un consentimiento activo específico por cliente y tipo
     */
    public DataConsent getActiveConsent(String customerDocument, DataConsent.ConsentType consentType) {
        if (customerDocument == null || customerDocument.trim().isEmpty()) {
            throw new IllegalArgumentException("El documento del cliente es obligatorio");
        }
        if (consentType == null) {
            throw new IllegalArgumentException("El tipo de consentimiento es obligatorio");
        }
        
        return dataConsentRepository
            .findByCustomerDocumentAndConsentTypeAndIsActiveTrue(customerDocument.trim(), consentType);
    }
    
    /**
     * Encuentra consentimientos que han expirado o están próximos a expirar
     */
    public List<DataConsent> findExpiredConsents() {
        return dataConsentRepository.findByRetentionUntilBeforeAndIsActiveTrue(LocalDateTime.now());
    }
    
    /**
     * Encuentra consentimientos que expirarán en los próximos días
     */
    public List<DataConsent> findConsentsExpiringInDays(int days) {
        LocalDateTime expirationThreshold = LocalDateTime.now().plusDays(days);
        return dataConsentRepository.findByRetentionUntilBeforeAndIsActiveTrueOrderByRetentionUntilAsc(expirationThreshold);
    }
    
    /**
     * Cuenta consentimientos por tipo
     */
    public long countByConsentType(DataConsent.ConsentType consentType) {
        return dataConsentRepository.countByConsentTypeAndIsActiveTrue(consentType);
    }
    
    /**
     * Encuentra consentimientos por base legal
     */
    public List<DataConsent> findByLegalBasis(DataConsent.LegalBasis legalBasis) {
        if (legalBasis == null) {
            throw new IllegalArgumentException("La base legal es obligatoria");
        }
        return dataConsentRepository.findByLegalBasisAndIsActiveTrueOrderByGrantedAtDesc(legalBasis);
    }
    
    /**
     * Encuentra consentimientos otorgados en un rango de fechas
     */
    public List<DataConsent> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return dataConsentRepository.findByGrantedAtBetweenOrderByGrantedAtDesc(startDate, endDate);
    }
    
    /**
     * Busca un consentimiento por su ID
     */
    public DataConsent findById(Long consentId) {
        return dataConsentRepository.findById(consentId).orElse(null);
    }
    
    /**
     * Obtiene todos los consentimientos activos
     */
    public List<DataConsent> findAllActiveConsents() {
        return dataConsentRepository.findByIsActiveTrueOrderByGrantedAtDesc();
    }
    
    /**
     * Encuentra consentimientos revocados en un rango de fechas
     */
    public List<DataConsent> findRevokedInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return dataConsentRepository.findByIsActiveFalseAndRevokedAtBetweenOrderByRevokedAtDesc(startDate, endDate);
    }
}
