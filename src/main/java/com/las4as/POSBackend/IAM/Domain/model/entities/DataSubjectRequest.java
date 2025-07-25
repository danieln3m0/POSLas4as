package com.las4as.POSBackend.IAM.Domain.model.entities;

import com.las4as.POSBackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "data_subject_requests")
@Getter
@NoArgsConstructor
public class DataSubjectRequest extends AuditableModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(name = "customer_document", nullable = false, length = 20)
    private String customerDocument;
    
    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;
    
    @Column(name = "customer_email", length = 100)
    private String customerEmail;
    
    @Column(name = "customer_phone", length = 20)
    private String customerPhone;
    
    @Column(name = "request_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RequestType requestType;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "status", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private RequestStatus status;
    
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "assigned_to_user_id")
    private Long assignedToUserId;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "processed_by_user_id")
    private Long processedByUserId;
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
    
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;
    
    @Column(name = "priority", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Priority priority;
    
    public DataSubjectRequest(String customerDocument, String customerName, String customerEmail,
                             String customerPhone, RequestType requestType, String description,
                             String ipAddress) {
        this.customerDocument = customerDocument;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.requestType = requestType;
        this.description = description;
        this.ipAddress = ipAddress;
        this.status = RequestStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
        this.priority = Priority.MEDIUM;
        
        // Establecer fecha límite según la Ley 29733 (30 días calendario)
        this.dueDate = LocalDateTime.now().plusDays(30);
    }
    
    public void assignTo(Long userId) {
        this.assignedToUserId = userId;
        this.status = RequestStatus.IN_PROGRESS;
    }
    
    public void complete(Long processedByUserId, String resolutionNotes) {
        this.processedByUserId = processedByUserId;
        this.processedAt = LocalDateTime.now();
        this.resolutionNotes = resolutionNotes;
        this.status = RequestStatus.COMPLETED;
    }
    
    public void reject(Long processedByUserId, String resolutionNotes) {
        this.processedByUserId = processedByUserId;
        this.processedAt = LocalDateTime.now();
        this.resolutionNotes = resolutionNotes;
        this.status = RequestStatus.REJECTED;
    }
    
    public boolean isOverdue() {
        return LocalDateTime.now().isAfter(dueDate) && status != RequestStatus.COMPLETED;
    }
    
    public enum RequestType {
        ACCESS,        // Derecho de acceso (Art. 18° Ley 29733)
        RECTIFICATION, // Derecho de rectificación (Art. 19° Ley 29733)
        CANCELLATION,  // Derecho de cancelación (Art. 20° Ley 29733)
        OPPOSITION     // Derecho de oposición (Art. 21° Ley 29733)
    }
    
    public enum RequestStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        REJECTED,
        EXPIRED
    }
    
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
}
