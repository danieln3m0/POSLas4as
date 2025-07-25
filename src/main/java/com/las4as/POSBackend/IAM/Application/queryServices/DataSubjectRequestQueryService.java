package com.las4as.POSBackend.IAM.Application.queryServices;

import com.las4as.POSBackend.IAM.Domain.model.entities.DataSubjectRequest;
import com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories.DataSubjectRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio de consultas para solicitudes ARCO (HU4.6)
 * Implementa las operaciones de lectura y búsqueda de solicitudes de derechos ARCO
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DataSubjectRequestQueryService {
    
    private final DataSubjectRequestRepository dataSubjectRequestRepository;
    
    /**
     * Busca una solicitud por su ID
     */
    public DataSubjectRequest findById(Long requestId) {
        return dataSubjectRequestRepository.findById(requestId)
            .orElse(null);
    }
    
    /**
     * Encuentra todas las solicitudes pendientes
     */
    public List<DataSubjectRequest> findAllPending() {
        return dataSubjectRequestRepository.findByStatus(DataSubjectRequest.RequestStatus.PENDING);
    }
    
    /**
     * Encuentra solicitudes por estado
     */
    public List<DataSubjectRequest> findByStatus(String status) {
        try {
            DataSubjectRequest.RequestStatus requestStatus = DataSubjectRequest.RequestStatus.valueOf(status.toUpperCase());
            return dataSubjectRequestRepository.findByStatus(requestStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado de solicitud inválido: " + status);
        }
    }
    
    /**
     * Encuentra solicitudes por tipo de derecho
     */
    public List<DataSubjectRequest> findByRequestType(String requestType) {
        try {
            DataSubjectRequest.RequestType type = DataSubjectRequest.RequestType.valueOf(requestType.toUpperCase());
            return dataSubjectRequestRepository.findByRequestType(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de solicitud inválido: " + requestType);
        }
    }
    
    /**
     * Encuentra solicitudes vencidas (que han excedido su fecha límite)
     */
    public List<DataSubjectRequest> findOverdueRequests() {
        return dataSubjectRequestRepository.findByDueDateBeforeAndStatusIn(
            LocalDateTime.now(),
            List.of(DataSubjectRequest.RequestStatus.PENDING, DataSubjectRequest.RequestStatus.IN_PROGRESS)
        );
    }
    
    /**
     * Encuentra solicitudes por documento del cliente
     */
    public List<DataSubjectRequest> findByCustomerDocument(String customerDocument) {
        if (customerDocument == null || customerDocument.trim().isEmpty()) {
            throw new IllegalArgumentException("El documento del cliente es obligatorio");
        }
        return dataSubjectRequestRepository.findByCustomerDocumentOrderByRequestedAtDesc(customerDocument.trim());
    }
    
    /**
     * Encuentra solicitudes asignadas a un usuario específico
     */
    public List<DataSubjectRequest> findAssignedToUser(Long userId) {
        return dataSubjectRequestRepository.findByAssignedToUserIdOrderByDueDateAsc(userId);
    }
    
    /**
     * Cuenta solicitudes por estado
     */
    public long countByStatus(DataSubjectRequest.RequestStatus status) {
        return dataSubjectRequestRepository.countByStatus(status);
    }
    
    /**
     * Encuentra solicitudes creadas en un rango de fechas
     */
    public List<DataSubjectRequest> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return dataSubjectRequestRepository.findByRequestedAtBetweenOrderByRequestedAtDesc(startDate, endDate);
    }
    
    /**
     * Encuentra solicitudes que requieren atención urgente (próximas a vencer)
     */
    public List<DataSubjectRequest> findUrgentRequests() {
        LocalDateTime urgentThreshold = LocalDateTime.now().plusDays(3); // 3 días antes del vencimiento
        return dataSubjectRequestRepository.findByDueDateBeforeAndStatusIn(
            urgentThreshold,
            List.of(DataSubjectRequest.RequestStatus.PENDING, DataSubjectRequest.RequestStatus.IN_PROGRESS)
        );
    }
}
