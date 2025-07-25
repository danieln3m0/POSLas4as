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
    
    @Query("SELECT dc FROM DataConsent dc WHERE dc.customerDocument = :customerDocument AND dc.isGranted = true AND dc.isRevoked = false ORDER BY dc.grantedAt DESC")
    List<DataConsent> findByCustomerDocumentAndIsActiveTrueOrderByGrantedAtDesc(@Param("customerDocument") String customerDocument);
    
    @Query("SELECT dc FROM DataConsent dc WHERE dc.customerDocument = :customerDocument AND dc.consentType = :consentType AND dc.isGranted = true AND dc.isRevoked = false")
    DataConsent findByCustomerDocumentAndConsentTypeAndIsActiveTrue(
        @Param("customerDocument") String customerDocument, 
        @Param("consentType") DataConsent.ConsentType consentType
    );
    
    @Query("SELECT dc FROM DataConsent dc WHERE dc.grantedAt < :currentDate AND dc.isGranted = true AND dc.isRevoked = false AND dc.retentionPeriodMonths IS NOT NULL")
    List<DataConsent> findByRetentionUntilBeforeAndIsActiveTrue(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT dc FROM DataConsent dc WHERE dc.grantedAt < :expirationThreshold AND dc.isGranted = true AND dc.isRevoked = false AND dc.retentionPeriodMonths IS NOT NULL ORDER BY dc.grantedAt ASC")
    List<DataConsent> findByRetentionUntilBeforeAndIsActiveTrueOrderByRetentionUntilAsc(@Param("expirationThreshold") LocalDateTime expirationThreshold);
    
    @Query("SELECT COUNT(dc) FROM DataConsent dc WHERE dc.consentType = :consentType AND dc.isGranted = true AND dc.isRevoked = false")
    long countByConsentTypeAndIsActiveTrue(@Param("consentType") DataConsent.ConsentType consentType);
    
    @Query("SELECT dc FROM DataConsent dc WHERE dc.legalBasis = :legalBasis AND dc.isGranted = true AND dc.isRevoked = false ORDER BY dc.grantedAt DESC")
    List<DataConsent> findByLegalBasisAndIsActiveTrueOrderByGrantedAtDesc(@Param("legalBasis") DataConsent.LegalBasis legalBasis);
    
    List<DataConsent> findByGrantedAtBetweenOrderByGrantedAtDesc(
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    @Query("SELECT dc FROM DataConsent dc WHERE dc.isGranted = true AND dc.isRevoked = false ORDER BY dc.grantedAt DESC")
    List<DataConsent> findByIsActiveTrueOrderByGrantedAtDesc();
    
    @Query("SELECT dc FROM DataConsent dc WHERE (dc.isGranted = false OR dc.isRevoked = true) AND dc.revokedAt BETWEEN :startDate AND :endDate ORDER BY dc.revokedAt DESC")
    List<DataConsent> findByIsActiveFalseAndRevokedAtBetweenOrderByRevokedAtDesc(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
}
