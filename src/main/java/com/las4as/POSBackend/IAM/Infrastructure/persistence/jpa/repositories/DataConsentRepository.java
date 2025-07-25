package com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories;

import com.las4as.POSBackend.IAM.Domain.model.entities.DataConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DataConsentRepository extends JpaRepository<DataConsent, Long> {
    
    List<DataConsent> findByCustomerId(Long customerId);
    
    List<DataConsent> findByCustomerDocument(String customerDocument);
    
    Optional<DataConsent> findByCustomerIdAndConsentTypeAndIsRevokedFalse(
        Long customerId, DataConsent.ConsentType consentType);
    
    @Query("SELECT dc FROM DataConsent dc WHERE dc.customerId = :customerId AND dc.isGranted = true AND dc.isRevoked = false")
    List<DataConsent> findActiveConsentsByCustomer(@Param("customerId") Long customerId);
    
    @Query("SELECT dc FROM DataConsent dc WHERE dc.consentType = :consentType AND dc.isGranted = true AND dc.isRevoked = false")
    List<DataConsent> findActiveConsentsByType(@Param("consentType") DataConsent.ConsentType consentType);
    
    @Query("SELECT dc FROM DataConsent dc WHERE dc.grantedAt < :expirationDate AND dc.isRevoked = false AND dc.retentionPeriodMonths IS NOT NULL")
    List<DataConsent> findExpiredConsents(@Param("expirationDate") LocalDateTime expirationDate);
    
    @Query("SELECT COUNT(dc) FROM DataConsent dc WHERE dc.consentType = :consentType AND dc.isGranted = true AND dc.isRevoked = false")
    Long countActiveConsentsByType(@Param("consentType") DataConsent.ConsentType consentType);
    
    // Métodos adicionales requeridos por DataConsentQueryService
    List<DataConsent> findByCustomerDocumentOrderByGrantedAtDesc(String customerDocument);
    
    List<DataConsent> findByCustomerDocumentAndIsActiveTrueOrderByGrantedAtDesc(String customerDocument);
    
    DataConsent findByCustomerDocumentAndConsentTypeAndIsActiveTrue(
        String customerDocument, 
        DataConsent.ConsentType consentType
    );
    
    List<DataConsent> findByRetentionUntilBeforeAndIsActiveTrue(LocalDateTime currentDate);
    
    List<DataConsent> findByRetentionUntilBeforeAndIsActiveTrueOrderByRetentionUntilAsc(LocalDateTime expirationThreshold);
    
    long countByConsentTypeAndIsActiveTrue(DataConsent.ConsentType consentType);
    
    List<DataConsent> findByLegalBasisAndIsActiveTrueOrderByGrantedAtDesc(DataConsent.LegalBasis legalBasis);
    
    List<DataConsent> findByGrantedAtBetweenOrderByGrantedAtDesc(
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    List<DataConsent> findByIsActiveTrueOrderByGrantedAtDesc();
    
    List<DataConsent> findByIsActiveFalseAndRevokedAtBetweenOrderByRevokedAtDesc(
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
}
