package com.las4as.POSBackend.IAM.Interfaces.resources;

import com.las4as.POSBackend.IAM.Application.commandServices.DataConsentCommandService;
import com.las4as.POSBackend.IAM.Application.queryServices.DataConsentQueryService;
import com.las4as.POSBackend.IAM.Domain.model.entities.DataConsent;
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
@RequestMapping("/api/v1/data-consent")
@RequiredArgsConstructor
@Tag(name = "Gestión de Consentimiento de Datos", description = "Endpoints para gestionar el consentimiento de datos personales según Ley N° 29733 (HU4.5)")
@SecurityRequirement(name = "Bearer Authentication")
public class DataConsentResource {
    
    private final DataConsentCommandService dataConsentCommandService;
    private final DataConsentQueryService dataConsentQueryService;
    
    @PostMapping
    @PreAuthorize("hasRole('CASHIER') or hasRole('ADMIN')")
    @Operation(
        summary = "Registrar consentimiento de datos (HU4.5)", 
        description = "Registra el consentimiento explícito del cliente para el tratamiento de sus datos personales según la Ley N° 29733"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Consentimiento registrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Consentimiento registrado",
                    summary = "Respuesta exitosa de registro de consentimiento",
                    value = """
                    {
                      "success": true,
                      "message": "Consentimiento registrado exitosamente",
                      "code": "CONSENT_REGISTERED",
                      "data": {
                        "id": 1,
                        "customerDocument": "12345678",
                        "customerName": "Juan Pérez",
                        "consentType": "SALES_PROCESSING",
                        "isGranted": true,
                        "grantedAt": "2024-07-24T14:30:15"
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos de consentimiento inválidos"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "No tiene permisos para registrar consentimientos"
        )
    })
    public ResponseEntity<ApiResponse<DataConsent>> registerConsent(
            @RequestBody RegisterConsentRequest request, 
            HttpServletRequest httpRequest) {
        
        try {
            String ipAddress = getClientIpAddress(httpRequest);
            
            DataConsent consent = dataConsentCommandService.registerConsent(
                request.getCustomerDocument(), 
                request.getCustomerName(),
                null, // customerEmail no disponible en el request actual
                DataConsent.ConsentType.valueOf(request.getConsentType()),
                DataConsent.LegalBasis.valueOf(request.getLegalBasis()),
                request.getPurpose(),
                null, // retentionUntil - se calculará internamente 
                ipAddress,
                httpRequest.getHeader("User-Agent")
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Consentimiento registrado exitosamente", "CONSENT_REGISTERED", consent));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al registrar consentimiento", "CONSENT_ERROR"));
        }
    }
    
    @GetMapping("/customer/{customerDocument}")
    @PreAuthorize("hasRole('CASHIER') or hasRole('ADMIN')")
    @Operation(
        summary = "Consultar consentimientos de cliente", 
        description = "Obtiene todos los consentimientos registrados para un cliente específico"
    )
    public ResponseEntity<ApiResponse<List<DataConsent>>> getCustomerConsents(
            @PathVariable @Parameter(description = "Documento del cliente (DNI/RUC)", example = "12345678") String customerDocument) {
        
        try {
            List<DataConsent> consents = dataConsentQueryService.findByCustomerDocument(customerDocument);
            
            return ResponseEntity.ok(
                ApiResponse.success("Consentimientos obtenidos exitosamente", "CONSENTS_FOUND", consents)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al obtener consentimientos", "CONSENT_QUERY_ERROR"));
        }
    }
    
    @PutMapping("/{consentId}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Revocar consentimiento", 
        description = "Revoca un consentimiento previamente otorgado por el cliente"
    )
    public ResponseEntity<ApiResponse<DataConsent>> revokeConsent(
            @PathVariable @Parameter(description = "ID del consentimiento", example = "1") Long consentId,
            @RequestBody RevokeConsentRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            String ipAddress = getClientIpAddress(httpRequest);
            DataConsent consent = dataConsentCommandService.revokeConsent(consentId, request.getReason(), ipAddress);
            
            return ResponseEntity.ok(
                ApiResponse.success("Consentimiento revocado exitosamente", "CONSENT_REVOKED", consent)
            );
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al revocar consentimiento", "REVOKE_ERROR"));
        }
    }
    
    @GetMapping("/check/{customerDocument}/{consentType}")
    @PreAuthorize("hasRole('CASHIER') or hasRole('ADMIN')")
    @Operation(
        summary = "Verificar consentimiento activo", 
        description = "Verifica si un cliente tiene consentimiento activo para un tipo específico de tratamiento"
    )
    public ResponseEntity<ApiResponse<ConsentCheckResult>> checkConsent(
            @PathVariable @Parameter(description = "Documento del cliente", example = "12345678") String customerDocument,
            @PathVariable @Parameter(description = "Tipo de consentimiento", example = "SALES_PROCESSING") String consentType) {
        
        try {
            DataConsent.ConsentType consentTypeEnum = DataConsent.ConsentType.valueOf(consentType.toUpperCase());
            boolean hasConsent = dataConsentQueryService.hasActiveConsent(customerDocument, consentTypeEnum);
            
            ConsentCheckResult result = new ConsentCheckResult();
            result.setCustomerDocument(customerDocument);
            result.setConsentType(consentType);
            result.setHasActiveConsent(hasConsent);
            result.setMessage(hasConsent ? 
                "Cliente tiene consentimiento activo" : 
                "Cliente no tiene consentimiento activo para este tratamiento");
            
            return ResponseEntity.ok(
                ApiResponse.success("Verificación de consentimiento completada", "CONSENT_CHECKED", result)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al verificar consentimiento", "CONSENT_CHECK_ERROR"));
        }
    }
    
    @GetMapping("/expired")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Consentimientos expirados", 
        description = "Obtiene lista de consentimientos que han expirado según su período de retención"
    )
    public ResponseEntity<ApiResponse<List<DataConsent>>> getExpiredConsents() {
        try {
            List<DataConsent> expiredConsents = dataConsentQueryService.findExpiredConsents();
            
            return ResponseEntity.ok(
                ApiResponse.success("Consentimientos expirados obtenidos", "EXPIRED_CONSENTS_FOUND", expiredConsents)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al obtener consentimientos expirados", "EXPIRED_CONSENTS_ERROR"));
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
    
    // DTOs
    @Schema(description = "Request para registrar consentimiento de datos")
    public static class RegisterConsentRequest {
        @Schema(description = "ID del cliente (opcional)", example = "1")
        private Long customerId;
        
        @Schema(description = "Documento del cliente (DNI/RUC)", example = "12345678", required = true)
        private String customerDocument;
        
        @Schema(description = "Nombre completo del cliente", example = "Juan Pérez", required = true)
        private String customerName;
        
        @Schema(description = "Tipo de consentimiento", example = "SALES_PROCESSING", required = true,
                allowableValues = {"SALES_PROCESSING", "MARKETING_COMMUNICATIONS", "DATA_ANALYTICS", "LEGAL_COMPLIANCE", "CUSTOMER_SERVICE"})
        private String consentType;
        
        @Schema(description = "Propósito del tratamiento de datos", example = "Procesamiento de ventas y facturación", required = true)
        private String purpose;
        
        @Schema(description = "Base legal del tratamiento", example = "CONSENT", required = true,
                allowableValues = {"CONSENT", "CONTRACT", "LEGAL_OBLIGATION", "VITAL_INTERESTS", "PUBLIC_TASK", "LEGITIMATE_INTERESTS"})
        private String legalBasis;
        
        @Schema(description = "Período de retención en meses", example = "60")
        private Integer retentionPeriodMonths;
        
        // Getters y setters
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        
        public String getCustomerDocument() { return customerDocument; }
        public void setCustomerDocument(String customerDocument) { this.customerDocument = customerDocument; }
        
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        
        public String getConsentType() { return consentType; }
        public void setConsentType(String consentType) { this.consentType = consentType; }
        
        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
        
        public String getLegalBasis() { return legalBasis; }
        public void setLegalBasis(String legalBasis) { this.legalBasis = legalBasis; }
        
        public Integer getRetentionPeriodMonths() { return retentionPeriodMonths; }
        public void setRetentionPeriodMonths(Integer retentionPeriodMonths) { this.retentionPeriodMonths = retentionPeriodMonths; }
    }
    
    @Schema(description = "Request para revocar consentimiento")
    public static class RevokeConsentRequest {
        @Schema(description = "Razón de la revocación", example = "Solicitud del cliente", required = true)
        private String reason;
        
        // Getters y setters
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    @Schema(description = "Resultado de verificación de consentimiento")
    public static class ConsentCheckResult {
        @Schema(description = "Documento del cliente", example = "12345678")
        private String customerDocument;
        
        @Schema(description = "Tipo de consentimiento verificado", example = "SALES_PROCESSING")
        private String consentType;
        
        @Schema(description = "Indica si tiene consentimiento activo", example = "true")
        private boolean hasActiveConsent;
        
        @Schema(description = "Mensaje descriptivo", example = "Cliente tiene consentimiento activo")
        private String message;
        
        // Getters y setters
        public String getCustomerDocument() { return customerDocument; }
        public void setCustomerDocument(String customerDocument) { this.customerDocument = customerDocument; }
        
        public String getConsentType() { return consentType; }
        public void setConsentType(String consentType) { this.consentType = consentType; }
        
        public boolean isHasActiveConsent() { return hasActiveConsent; }
        public void setHasActiveConsent(boolean hasActiveConsent) { this.hasActiveConsent = hasActiveConsent; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
