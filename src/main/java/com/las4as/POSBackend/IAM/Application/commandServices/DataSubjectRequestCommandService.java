package com.las4as.POSBackend.IAM.Application.commandServices;

import com.las4as.POSBackend.IAM.Domain.model.entities.AuditLog;
import com.las4as.POSBackend.IAM.Domain.model.entities.DataSubjectRequest;
import com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories.DataSubjectRequestRepository;
import com.las4as.POSBackend.IAM.Infrastructure.authorization.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de comandos para gestión de solicitudes ARCO (HU4.6)
 * Implementa las operaciones de creación y modificación de solicitudes de derechos ARCO
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DataSubjectRequestCommandService {
    
    private final DataSubjectRequestRepository dataSubjectRequestRepository;
    private final AuditCommandService auditCommandService;
    
    /**
     * Crea una nueva solicitud de derechos ARCO
     */
    public DataSubjectRequest createRequest(
            String customerDocument,
            String customerName,
            String customerEmail,
            String customerPhone,
            DataSubjectRequest.RequestType requestType,
            String description,
            String ipAddress) {
        
        // Validaciones
        if (customerDocument == null || customerDocument.trim().isEmpty()) {
            throw new IllegalArgumentException("El documento del cliente es obligatorio");
        }
        
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio");
        }
        
        if (requestType == null) {
            throw new IllegalArgumentException("El tipo de solicitud es obligatorio");
        }
        
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción de la solicitud es obligatoria");
        }
        
        // Crear nueva solicitud
        DataSubjectRequest request = new DataSubjectRequest(
            customerDocument.trim(),
            customerName.trim(),
            customerEmail != null ? customerEmail.trim() : null,
            customerPhone != null ? customerPhone.trim() : null,
            requestType,
            description.trim(),
            ipAddress
        );
        
        // Guardar solicitud
        DataSubjectRequest savedRequest = dataSubjectRequestRepository.save(request);
        
        // Registrar en auditoría
        auditCommandService.logAction(
            null, // Usuario anónimo para solicitudes públicas
            "anonymous",
            "CREATE_DATA_SUBJECT_REQUEST",
            "DataSubjectRequest",
            savedRequest.getId(),
            null,
            String.format("Nueva solicitud ARCO de tipo %s para documento %s", 
                requestType, customerDocument),
            ipAddress,
            null,
            AuditLog.AuditSeverity.MEDIUM,
            "Solicitud ARCO creada por cliente"
        );
        
        return savedRequest;
    }
    
    /**
     * Asigna una solicitud ARCO a un usuario responsable
     */
    public DataSubjectRequest assignRequest(Long requestId, Long assignedToUserId) {
        DataSubjectRequest request = dataSubjectRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        
        if (request.getStatus() != DataSubjectRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException("Solo se pueden asignar solicitudes pendientes");
        }
        
        request.assignTo(assignedToUserId);
        
        DataSubjectRequest savedRequest = dataSubjectRequestRepository.save(request);
        
        // Registrar en auditoría
        auditCommandService.logAction(
            assignedToUserId,
            SecurityContextUtils.getCurrentUsername(),
            "ASSIGN_DATA_SUBJECT_REQUEST",
            "DataSubjectRequest",
            requestId,
            null,
            String.format("Solicitud ARCO #%d asignada al usuario %d", requestId, assignedToUserId),
            null,
            null,
            AuditLog.AuditSeverity.MEDIUM,
            "Solicitud ARCO asignada"
        );
        
        return savedRequest;
    }
    
    /**
     * Completa una solicitud ARCO con las acciones tomadas
     */
    public DataSubjectRequest completeRequest(Long requestId, Long completedByUserId, String resolutionNotes) {
        DataSubjectRequest request = dataSubjectRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        
        if (request.getStatus() != DataSubjectRequest.RequestStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Solo se pueden completar solicitudes en progreso");
        }
        
        if (resolutionNotes == null || resolutionNotes.trim().isEmpty()) {
            throw new IllegalArgumentException("Las notas de resolución son obligatorias");
        }
        
        request.complete(completedByUserId, resolutionNotes.trim());
        
        DataSubjectRequest savedRequest = dataSubjectRequestRepository.save(request);
        
        // Registrar en auditoría
        auditCommandService.logAction(
            completedByUserId,
            SecurityContextUtils.getCurrentUsername(),
            "COMPLETE_DATA_SUBJECT_REQUEST",
            "DataSubjectRequest",
            requestId,
            null,
            String.format("Solicitud ARCO #%d completada: %s", requestId, resolutionNotes),
            null,
            null,
            AuditLog.AuditSeverity.HIGH,
            "Solicitud ARCO completada"
        );
        
        return savedRequest;
    }
    
    /**
     * Rechaza una solicitud ARCO con justificación
     */
    public DataSubjectRequest rejectRequest(Long requestId, Long rejectedByUserId, String rejectionReason) {
        DataSubjectRequest request = dataSubjectRequestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        
        if (request.getStatus() == DataSubjectRequest.RequestStatus.COMPLETED ||
            request.getStatus() == DataSubjectRequest.RequestStatus.REJECTED) {
            throw new IllegalArgumentException("No se puede rechazar una solicitud ya procesada");
        }
        
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new IllegalArgumentException("La razón del rechazo es obligatoria");
        }
        
        request.reject(rejectedByUserId, rejectionReason.trim());
        
        DataSubjectRequest savedRequest = dataSubjectRequestRepository.save(request);
        
        // Registrar en auditoría
        auditCommandService.logAction(
            rejectedByUserId,
            SecurityContextUtils.getCurrentUsername(),
            "REJECT_DATA_SUBJECT_REQUEST",
            "DataSubjectRequest",
            requestId,
            null,
            String.format("Solicitud ARCO #%d rechazada: %s", requestId, rejectionReason),
            null,
            null,
            AuditLog.AuditSeverity.HIGH,
            "Solicitud ARCO rechazada"
        );
        
        return savedRequest;
    }
}
