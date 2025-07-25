package com.las4as.POSBackend.IAM.Interfaces.resources;

import com.las4as.POSBackend.IAM.Application.queryServices.AuditQueryService;
import com.las4as.POSBackend.IAM.Domain.model.entities.AuditLog;
import com.las4as.POSBackend.shared.interfaces.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
@Tag(name = "Auditoría del Sistema", description = "Endpoints para consultar registros de auditoría y actividad de usuarios (HU4.4)")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditResource {
    
    private final AuditQueryService auditQueryService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener registros de auditoría (HU4.4)", 
        description = "Obtiene registros detallados de las acciones realizadas por los usuarios para fines de auditoría y seguridad"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Registros de auditoría obtenidos exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Registros de auditoría",
                    summary = "Lista paginada de eventos de auditoría",
                    value = """
                    {
                      "success": true,
                      "data": {
                        "content": [
                          {
                            "id": 1,
                            "userId": 2,
                            "username": "mcajero",
                            "action": "SALE_CREATE",
                            "entityType": "SALE",
                            "entityId": 15,
                            "actionTimestamp": "2024-07-24T14:30:15",
                            "severity": "MEDIUM",
                            "description": "Nueva venta registrada",
                            "ipAddress": "192.168.1.100"
                          }
                        ],
                        "totalElements": 150,
                        "totalPages": 15,
                        "currentPage": 0
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "No tiene permisos de administrador"
        )
    })
    public ResponseEntity<ApiResponse<Page<AuditLog>>> getAuditLogs(
            @RequestParam(defaultValue = "0") @Parameter(description = "Página a consultar", example = "0") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Tamaño de página", example = "20") int size,
            @RequestParam(required = false) @Parameter(description = "Filtrar por usuario", example = "mcajero") String username,
            @RequestParam(required = false) @Parameter(description = "Filtrar por acción", example = "SALE_CREATE") String action,
            @RequestParam(required = false) @Parameter(description = "Filtrar por severidad", example = "HIGH") AuditLog.AuditSeverity severity,
            @RequestParam(required = false) @Parameter(description = "Fecha inicio (ISO)", example = "2024-07-01T00:00:00") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @Parameter(description = "Fecha fin (ISO)", example = "2024-07-31T23:59:59") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AuditLog> auditLogs = auditQueryService.findAuditLogs(
                username, action, severity, startDate, endDate, pageable
            );
            
            return ResponseEntity.ok(
                ApiResponse.success("Registros de auditoría obtenidos exitosamente", "AUDIT_LOGS_FOUND", auditLogs)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Error al obtener registros de auditoría", "AUDIT_QUERY_ERROR"));
        }
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Actividad de usuario específico (HU4.4)", 
        description = "Obtiene el historial detallado de actividades de un usuario específico"
    )
    public ResponseEntity<ApiResponse<List<AuditLog>>> getUserActivity(
            @PathVariable @Parameter(description = "ID del usuario", example = "2") Long userId,
            @RequestParam(defaultValue = "50") @Parameter(description = "Límite de registros", example = "50") int limit) {
        
        try {
            List<AuditLog> userActivity = auditQueryService.getUserActivity(userId, limit);
            
            return ResponseEntity.ok(
                ApiResponse.success("Actividad del usuario obtenida exitosamente", "USER_ACTIVITY_FOUND", userActivity)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Error al obtener actividad del usuario", "USER_ACTIVITY_ERROR"));
        }
    }
    
    @GetMapping("/critical")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Eventos críticos de seguridad (HU4.4)", 
        description = "Obtiene eventos críticos y violaciones de seguridad para revisión prioritaria"
    )
    public ResponseEntity<ApiResponse<List<AuditLog>>> getCriticalEvents(
            @RequestParam(defaultValue = "24") @Parameter(description = "Horas hacia atrás", example = "24") int hoursBack) {
        
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
            List<AuditLog> criticalEvents = auditQueryService.getCriticalEventsSince(since);
            
            return ResponseEntity.ok(
                ApiResponse.success("Eventos críticos obtenidos exitosamente", "CRITICAL_EVENTS_FOUND", criticalEvents)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Error al obtener eventos críticos", "CRITICAL_EVENTS_ERROR"));
        }
    }
    
    @GetMapping("/login-attempts/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Intentos de login de usuario", 
        description = "Obtiene el historial de intentos de autenticación de un usuario específico"
    )
    public ResponseEntity<ApiResponse<List<AuditLog>>> getLoginAttempts(
            @PathVariable @Parameter(description = "ID del usuario", example = "2") Long userId) {
        
        try {
            List<AuditLog> loginAttempts = auditQueryService.getLoginAttempts(userId);
            
            return ResponseEntity.ok(
                ApiResponse.success("Intentos de login obtenidos exitosamente", "LOGIN_ATTEMPTS_FOUND", loginAttempts)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Error al obtener intentos de login", "LOGIN_ATTEMPTS_ERROR"));
        }
    }
    
    @GetMapping("/sales-activity")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
        summary = "Actividad de ventas", 
        description = "Obtiene registros de auditoría relacionados con operaciones de ventas"
    )
    public ResponseEntity<ApiResponse<List<AuditLog>>> getSalesActivity(
            @RequestParam(required = false) @Parameter(description = "ID del cajero", example = "2") Long cashierId,
            @RequestParam(required = false) @Parameter(description = "Fecha inicio", example = "2024-07-01T00:00:00") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @Parameter(description = "Fecha fin", example = "2024-07-31T23:59:59") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<AuditLog> salesActivity = auditQueryService.getSalesActivity(cashierId, startDate, endDate);
            
            return ResponseEntity.ok(
                ApiResponse.success("Actividad de ventas obtenida exitosamente", "SALES_ACTIVITY_FOUND", salesActivity)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Error al obtener actividad de ventas", "SALES_ACTIVITY_ERROR"));
        }
    }
    
    @GetMapping("/inventory-changes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INVENTORY_MANAGER')")
    @Operation(
        summary = "Modificaciones de inventario", 
        description = "Obtiene registros de auditoría de cambios en el inventario y stock"
    )
    public ResponseEntity<ApiResponse<List<AuditLog>>> getInventoryChanges(
            @RequestParam(required = false) @Parameter(description = "ID del producto", example = "5") Long productId,
            @RequestParam(required = false) @Parameter(description = "Fecha inicio", example = "2024-07-01T00:00:00") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @Parameter(description = "Fecha fin", example = "2024-07-31T23:59:59") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        try {
            List<AuditLog> inventoryChanges = auditQueryService.getInventoryChanges(productId, startDate, endDate);
            
            return ResponseEntity.ok(
                ApiResponse.success("Modificaciones de inventario obtenidas exitosamente", "INVENTORY_CHANGES_FOUND", inventoryChanges)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Error al obtener modificaciones de inventario", "INVENTORY_CHANGES_ERROR"));
        }
    }
}
