package com.las4as.POSBackend.IAM.Interfaces.resources;

import com.las4as.POSBackend.IAM.Application.commandServices.DataSubjectRequestCommandService;
import com.las4as.POSBackend.IAM.Application.queryServices.DataSubjectRequestQueryService;
import com.las4as.POSBackend.IAM.Domain.model.entities.DataSubjectRequest;
import com.las4as.POSBackend.IAM.Infrastructure.authorization.SecurityContextUtils;
import com.las4as.POSBackend.shared.interfaces.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/data-rights")
@RequiredArgsConstructor
@Tag(name = "Derechos ARCO", description = "Endpoints para gestionar solicitudes de derechos de Acceso, Rectificación, Cancelación y Oposición según Ley N° 29733 (HU4.6)")
@SecurityRequirement(name = "Bearer Authentication")
public class DataRightsResource {
    
    private final DataSubjectRequestCommandService dataSubjectRequestCommandService;
    private final DataSubjectRequestQueryService dataSubjectRequestQueryService;
    
    @PostMapping("/request")
    @Operation(
        summary = "Solicitar ejercicio de derechos ARCO (HU4.6)", 
        description = "Permite a los clientes solicitar el ejercicio de sus derechos de Acceso, Rectificación, Cancelación u Oposición al tratamiento de datos personales"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Solicitud de derechos registrada exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Solicitud ARCO registrada",
                    summary = "Respuesta exitosa de registro de solicitud",
                    value = """
                    {
                      "success": true,
                      "message": "Solicitud de derechos registrada exitosamente",
                      "code": "ARCO_REQUEST_CREATED",
                      "data": {
                        "id": 1,
                        "customerDocument": "12345678",
                        "customerName": "Juan Pérez",
                        "requestType": "ACCESS",
                        "status": "PENDING",
                        "requestedAt": "2024-07-24T14:30:15",
                        "dueDate": "2024-08-23T14:30:15"
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos de solicitud inválidos"
        )
    })
    public ResponseEntity<ApiResponse<DataSubjectRequest>> createDataRightsRequest(
            @RequestBody CreateDataRightsRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            String ipAddress = getClientIpAddress(httpRequest);
            
            DataSubjectRequest dataRequest = dataSubjectRequestCommandService.createRequest(
                request.getCustomerDocument(),
                request.getCustomerName(),
                request.getCustomerEmail(),
                request.getCustomerPhone(),
                DataSubjectRequest.RequestType.valueOf(request.getRequestType()),
                request.getDescription(),
                ipAddress
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Solicitud de derechos registrada exitosamente", "ARCO_REQUEST_CREATED", dataRequest));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al registrar solicitud", "REQUEST_ERROR"));
        }
    }
    
    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DATA_PROTECTION_OFFICER')")
    @Operation(
        summary = "Listar solicitudes ARCO (HU4.6)", 
        description = "Obtiene la lista de solicitudes de derechos ARCO para gestión administrativa"
    )
    public ResponseEntity<ApiResponse<List<DataSubjectRequest>>> getDataRightsRequests(
            @RequestParam(required = false) @Parameter(description = "Filtrar por estado", example = "PENDING") String status,
            @RequestParam(required = false) @Parameter(description = "Filtrar por tipo", example = "ACCESS") String requestType,
            @RequestParam(defaultValue = "false") @Parameter(description = "Solo solicitudes vencidas", example = "false") boolean onlyOverdue) {
        
        try {
            List<DataSubjectRequest> requests;
            
            if (onlyOverdue) {
                requests = dataSubjectRequestQueryService.findOverdueRequests();
            } else if (status != null) {
                requests = dataSubjectRequestQueryService.findByStatus(status);
            } else if (requestType != null) {
                requests = dataSubjectRequestQueryService.findByRequestType(requestType);
            } else {
                requests = dataSubjectRequestQueryService.findAllPending();
            }
            
            return ResponseEntity.ok(
                ApiResponse.success("Solicitudes obtenidas exitosamente", "REQUESTS_FOUND", requests)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al obtener solicitudes", "REQUESTS_QUERY_ERROR"));
        }
    }
    
    @GetMapping("/requests/{requestId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DATA_PROTECTION_OFFICER')")
    @Operation(
        summary = "Obtener solicitud específica", 
        description = "Obtiene los detalles completos de una solicitud ARCO específica"
    )
    public ResponseEntity<ApiResponse<DataSubjectRequest>> getDataRightsRequest(
            @PathVariable @Parameter(description = "ID de la solicitud", example = "1") Long requestId) {
        
        try {
            DataSubjectRequest request = dataSubjectRequestQueryService.findById(requestId);
            if (request == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Solicitud no encontrada", "REQUEST_NOT_FOUND"));
            }
            
            return ResponseEntity.ok(
                ApiResponse.success("Solicitud encontrada", "REQUEST_FOUND", request)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al obtener solicitud", "REQUEST_QUERY_ERROR"));
        }
    }
    
    @PutMapping("/requests/{requestId}/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DATA_PROTECTION_OFFICER')")
    @Operation(
        summary = "Asignar solicitud ARCO", 
        description = "Asigna una solicitud ARCO a un usuario responsable para su procesamiento"
    )
    public ResponseEntity<ApiResponse<DataSubjectRequest>> assignRequest(
            @PathVariable @Parameter(description = "ID de la solicitud", example = "1") Long requestId,
            @RequestBody AssignRequestRequest request) {
        
        try {
            DataSubjectRequest updatedRequest = dataSubjectRequestCommandService.assignRequest(
                requestId, request.getAssignedToUserId()
            );
            
            return ResponseEntity.ok(
                ApiResponse.success("Solicitud asignada exitosamente", "REQUEST_ASSIGNED", updatedRequest)
            );
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al asignar solicitud", "ASSIGNMENT_ERROR"));
        }
    }
    
    @PutMapping("/requests/{requestId}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DATA_PROTECTION_OFFICER')")
    @Operation(
        summary = "Completar solicitud ARCO", 
        description = "Marca una solicitud ARCO como completada con las acciones tomadas"
    )
    public ResponseEntity<ApiResponse<DataSubjectRequest>> completeRequest(
            @PathVariable @Parameter(description = "ID de la solicitud", example = "1") Long requestId,
            @RequestBody CompleteRequestRequest request) {
        
        try {
            DataSubjectRequest completedRequest = dataSubjectRequestCommandService.completeRequest(
                requestId, getCurrentUserId(), request.getResolutionNotes()
            );
            
            return ResponseEntity.ok(
                ApiResponse.success("Solicitud completada exitosamente", "REQUEST_COMPLETED", completedRequest)
            );
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al completar solicitud", "COMPLETION_ERROR"));
        }
    }
    
    @PutMapping("/requests/{requestId}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DATA_PROTECTION_OFFICER')")
    @Operation(
        summary = "Rechazar solicitud ARCO", 
        description = "Rechaza una solicitud ARCO con justificación detallada"
    )
    public ResponseEntity<ApiResponse<DataSubjectRequest>> rejectRequest(
            @PathVariable @Parameter(description = "ID de la solicitud", example = "1") Long requestId,
            @RequestBody RejectRequestRequest request) {
        
        try {
            DataSubjectRequest rejectedRequest = dataSubjectRequestCommandService.rejectRequest(
                requestId, getCurrentUserId(), request.getRejectionReason()
            );
            
            return ResponseEntity.ok(
                ApiResponse.success("Solicitud rechazada", "REQUEST_REJECTED", rejectedRequest)
            );
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al rechazar solicitud", "REJECTION_ERROR"));
        }
    }
    
    @GetMapping("/customer/{customerDocument}")
    @Operation(
        summary = "Consultar mis solicitudes ARCO", 
        description = "Permite a un cliente consultar el estado de sus solicitudes ARCO usando su documento"
    )
    public ResponseEntity<ApiResponse<List<DataSubjectRequest>>> getCustomerRequests(
            @PathVariable @Parameter(description = "Documento del cliente", example = "12345678") String customerDocument) {
        
        try {
            List<DataSubjectRequest> requests = dataSubjectRequestQueryService.findByCustomerDocument(customerDocument);
            
            return ResponseEntity.ok(
                ApiResponse.success("Solicitudes del cliente obtenidas", "CUSTOMER_REQUESTS_FOUND", requests)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al obtener solicitudes del cliente", "CUSTOMER_REQUESTS_ERROR"));
        }
    }
    
    // Métodos auxiliares
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    private Long getCurrentUserId() {
        return SecurityContextUtils.getCurrentUserId();
    }
    
    // DTOs
    @Schema(description = "Request para crear solicitud de derechos ARCO")
    public static class CreateDataRightsRequest {
        @Schema(description = "Documento del cliente (DNI/RUC)", example = "12345678", required = true)
        private String customerDocument;
        
        @Schema(description = "Nombre completo del cliente", example = "Juan Pérez", required = true)
        private String customerName;
        
        @Schema(description = "Email del cliente", example = "juan.perez@email.com")
        private String customerEmail;
        
        @Schema(description = "Teléfono del cliente", example = "987654321")
        private String customerPhone;
        
        @Schema(description = "Tipo de derecho solicitado", example = "ACCESS", required = true,
                allowableValues = {"ACCESS", "RECTIFICATION", "CANCELLATION", "OPPOSITION"})
        private String requestType;
        
        @Schema(description = "Descripción detallada de la solicitud", example = "Solicito acceso a todos mis datos personales almacenados", required = true)
        private String description;
        
        // Getters y setters
        public String getCustomerDocument() { return customerDocument; }
        public void setCustomerDocument(String customerDocument) { this.customerDocument = customerDocument; }
        
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
        
        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
        
        public String getRequestType() { return requestType; }
        public void setRequestType(String requestType) { this.requestType = requestType; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    @Schema(description = "Request para asignar solicitud")
    public static class AssignRequestRequest {
        @Schema(description = "ID del usuario asignado", example = "3", required = true)
        private Long assignedToUserId;
        
        // Getters y setters
        public Long getAssignedToUserId() { return assignedToUserId; }
        public void setAssignedToUserId(Long assignedToUserId) { this.assignedToUserId = assignedToUserId; }
    }
    
    @Schema(description = "Request para completar solicitud")
    public static class CompleteRequestRequest {
        @Schema(description = "Notas de resolución", example = "Se proporcionó acceso completo a los datos personales del cliente", required = true)
        private String resolutionNotes;
        
        // Getters y setters
        public String getResolutionNotes() { return resolutionNotes; }
        public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    }
    
    @Schema(description = "Request para rechazar solicitud")
    public static class RejectRequestRequest {
        @Schema(description = "Razón del rechazo", example = "No se pudo verificar la identidad del solicitante", required = true)
        private String rejectionReason;
        
        // Getters y setters
        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    }
}
