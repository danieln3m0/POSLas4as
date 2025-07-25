package com.las4as.POSBackend.IAM.Application.commandServices;

import com.las4as.POSBackend.IAM.Domain.model.entities.AuditLog;
import com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditCommandService {
    
    private final AuditLogRepository auditLogRepository;
    
    public void logAction(Long userId, String username, String action, String entityType, 
                         Long entityId, String oldValues, String newValues, 
                         String ipAddress, String userAgent, AuditLog.AuditSeverity severity, 
                         String description) {
        
        AuditLog auditLog = new AuditLog(
            userId, username, action, entityType, entityId,
            oldValues, newValues, ipAddress, userAgent,
            severity, description
        );
        
        auditLogRepository.save(auditLog);
    }
    
    public void logLogin(Long userId, String username, String ipAddress, String userAgent, boolean successful) {
        String action = successful ? "LOGIN_SUCCESS" : "LOGIN_FAILED";
        AuditLog.AuditSeverity severity = successful ? AuditLog.AuditSeverity.LOW : AuditLog.AuditSeverity.MEDIUM;
        String description = successful ? "Usuario autenticado exitosamente" : "Intento de autenticación fallido";
        
        logAction(userId, username, action, "USER", userId, null, null, 
                 ipAddress, userAgent, severity, description);
    }
    
    public void logLogout(Long userId, String username, String ipAddress) {
        logAction(userId, username, "LOGOUT", "USER", userId, null, null, 
                 ipAddress, null, AuditLog.AuditSeverity.LOW, "Usuario cerró sesión");
    }
    
    public void logPasswordChange(Long userId, String username, String ipAddress) {
        logAction(userId, username, "PASSWORD_CHANGE", "USER", userId, null, null, 
                 ipAddress, null, AuditLog.AuditSeverity.HIGH, "Usuario cambió su contraseña");
    }
    
    public void logUserCreation(Long createdById, String createdByUsername, Long newUserId, 
                               String newUsername, String ipAddress) {
        logAction(createdById, createdByUsername, "USER_CREATE", "USER", newUserId, 
                 null, "Usuario: " + newUsername, ipAddress, null, 
                 AuditLog.AuditSeverity.HIGH, "Nuevo usuario creado");
    }
    
    public void logUserModification(Long modifiedById, String modifiedByUsername, Long userId, 
                                   String username, String oldValues, String newValues, String ipAddress) {
        logAction(modifiedById, modifiedByUsername, "USER_UPDATE", "USER", userId, 
                 oldValues, newValues, ipAddress, null, 
                 AuditLog.AuditSeverity.MEDIUM, "Usuario modificado");
    }
    
    public void logSaleCreation(Long userId, String username, Long saleId, String saleNumber, 
                               String customerInfo, String ipAddress) {
        logAction(userId, username, "SALE_CREATE", "SALE", saleId, 
                 null, "Venta: " + saleNumber + ", Cliente: " + customerInfo, 
                 ipAddress, null, AuditLog.AuditSeverity.MEDIUM, "Nueva venta registrada");
    }
    
    public void logStockModification(Long userId, String username, Long productId, 
                                    String productName, int oldStock, int newStock, 
                                    String operation, String ipAddress) {
        String oldValues = "Stock anterior: " + oldStock;
        String newValues = "Stock actual: " + newStock + ", Operación: " + operation;
        
        logAction(userId, username, "STOCK_UPDATE", "PRODUCT", productId, 
                 oldValues, newValues, ipAddress, null, 
                 AuditLog.AuditSeverity.MEDIUM, "Stock de producto modificado: " + productName);
    }
    
    public void logSecurityViolation(Long userId, String username, String violationType, 
                                    String description, String ipAddress, String userAgent) {
        logAction(userId, username, "SECURITY_VIOLATION", "SECURITY", null, 
                 null, violationType, ipAddress, userAgent, 
                 AuditLog.AuditSeverity.CRITICAL, description);
    }
}
