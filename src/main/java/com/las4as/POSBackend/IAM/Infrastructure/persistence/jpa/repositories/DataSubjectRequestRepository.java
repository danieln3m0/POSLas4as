package com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories;

import com.las4as.POSBackend.IAM.Domain.model.entities.DataSubjectRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DataSubjectRequestRepository extends JpaRepository<DataSubjectRequest, Long> {
    
    List<DataSubjectRequest> findByCustomerDocumentOrderByRequestedAtDesc(String customerDocument);
    
    List<DataSubjectRequest> findByStatusOrderByRequestedAtDesc(DataSubjectRequest.RequestStatus status);
    
    List<DataSubjectRequest> findByRequestTypeOrderByRequestedAtDesc(DataSubjectRequest.RequestType requestType);
    
    List<DataSubjectRequest> findByAssignedToUserIdOrderByRequestedAtDesc(Long assignedToUserId);
    
    Page<DataSubjectRequest> findByStatusAndDueDateBefore(
        DataSubjectRequest.RequestStatus status, LocalDateTime dueDate, Pageable pageable);
    
    @Query("SELECT dsr FROM DataSubjectRequest dsr WHERE dsr.status IN :statuses ORDER BY dsr.priority DESC, dsr.dueDate ASC")
    List<DataSubjectRequest> findByStatusInOrderByPriorityAndDueDate(@Param("statuses") List<DataSubjectRequest.RequestStatus> statuses);
    
    @Query("SELECT dsr FROM DataSubjectRequest dsr WHERE dsr.dueDate < :now AND dsr.status NOT IN ('COMPLETED', 'REJECTED')")
    List<DataSubjectRequest> findOverdueRequests(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(dsr) FROM DataSubjectRequest dsr WHERE dsr.status = :status")
    Long countByStatus(@Param("status") DataSubjectRequest.RequestStatus status);
    
    @Query("SELECT dsr FROM DataSubjectRequest dsr WHERE dsr.requestedAt >= :since AND dsr.requestedAt <= :until")
    List<DataSubjectRequest> findRequestsInPeriod(@Param("since") LocalDateTime since, @Param("until") LocalDateTime until);
    
    // Métodos adicionales requeridos por DataSubjectRequestQueryService
    List<DataSubjectRequest> findByStatus(DataSubjectRequest.RequestStatus status);
    
    List<DataSubjectRequest> findByRequestType(DataSubjectRequest.RequestType requestType);
    
    List<DataSubjectRequest> findByDueDateBeforeAndStatusIn(
        LocalDateTime dueDate, 
        List<DataSubjectRequest.RequestStatus> statuses
    );
    
    List<DataSubjectRequest> findByAssignedToUserIdOrderByDueDateAsc(Long userId);
    
    List<DataSubjectRequest> findByRequestedAtBetweenOrderByRequestedAtDesc(
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
}
