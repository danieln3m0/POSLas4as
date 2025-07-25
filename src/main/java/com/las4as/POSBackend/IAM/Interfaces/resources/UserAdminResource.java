package com.las4as.POSBackend.IAM.Interfaces.resources;

import com.las4as.POSBackend.IAM.Application.commandServices.AuditCommandService;
import com.las4as.POSBackend.IAM.Application.commandServices.CreateUserCommandService;
import com.las4as.POSBackend.IAM.Application.queryServices.UserQueryService;
import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import com.las4as.POSBackend.IAM.Domain.model.commands.CreateUserCommand;
import com.las4as.POSBackend.IAM.Domain.model.entities.Role;
import com.las4as.POSBackend.IAM.Domain.services.PasswordPolicyService;
import com.las4as.POSBackend.IAM.Infrastructure.authorization.SecurityContextUtils;
import com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories.RoleRepository;
import com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories.UserRepository;
import com.las4as.POSBackend.IAM.Interfaces.transform.UserDTO;
import com.las4as.POSBackend.IAM.Interfaces.transform.UserTransformer;
import com.las4as.POSBackend.shared.interfaces.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Administración de Usuarios", description = "Endpoints para la gestión completa de usuarios del sistema (Solo administradores)")
@SecurityRequirement(name = "Bearer Authentication")
public class UserAdminResource {
    
    private final CreateUserCommandService createUserCommandService;
    private final UserQueryService userQueryService;
    private final UserTransformer userTransformer;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordPolicyService passwordPolicyService;
    private final AuditCommandService auditCommandService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Crear usuario (HU4.1)", 
        description = "Crea un nuevo usuario en el sistema con validaciones de políticas de contraseña y asignación de roles",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del nuevo usuario a crear",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreateUserRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Cajero",
                        summary = "Usuario con rol de cajero",
                        value = """
                        {
                          "username": "mcajero",
                          "email": "maria.cajero@pos.com",
                          "password": "Secure123!@#",
                          "firstName": "María",
                          "lastName": "Cajero",
                          "roleNames": ["CASHIER"]
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Gerente de Inventario",
                        summary = "Usuario con rol de gerente de inventario",
                        value = """
                        {
                          "username": "jgerente",
                          "email": "juan.gerente@pos.com",
                          "password": "Manager456$%^",
                          "firstName": "Juan",
                          "lastName": "Gerente",
                          "roleNames": ["INVENTORY_MANAGER"]
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Administrador",
                        summary = "Usuario con rol de administrador",
                        value = """
                        {
                          "username": "admin2",
                          "email": "admin2@pos.com",
                          "password": "Admin789&*(",
                          "firstName": "Carlos",
                          "lastName": "Administrador",
                          "roleNames": ["ADMIN"]
                        }
                        """
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Usuario creado exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos inválidos (usuario/email duplicado, contraseña débil, etc.)",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "No tiene permisos de administrador"
        )
    })
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody CreateUserRequest request, HttpServletRequest httpRequest) {
        try {
            // Validar política de contraseñas
            PasswordPolicyService.PasswordValidationResult validation = 
                passwordPolicyService.validatePassword(request.getPassword());
            
            if (!validation.isValid()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Contraseña no cumple las políticas de seguridad: " + 
                          String.join(", ", validation.getViolations()), "WEAK_PASSWORD"));
            }
            
            // Verificar que los roles existen
            List<Role> roles = roleRepository.findByNameIn(request.getRoleNames());
            if (roles.size() != request.getRoleNames().size()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Uno o más roles especificados no existen", "INVALID_ROLES"));
            }
            
            CreateUserCommand command = new CreateUserCommand(
                request.getUsername(),
                request.getEmail(), 
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getRoleNames()
            );
            
            User user = createUserCommandService.execute(command);
            UserDTO userDTO = userTransformer.toDTO(user);
            
            // Auditoría
            auditCommandService.logUserCreation(
                getCurrentUserId(), getCurrentUsername(), user.getId(), 
                user.getUsername().toString(), getClientIpAddress(httpRequest)
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Usuario creado exitosamente", "USER_CREATED", userDTO));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error interno del servidor", "INTERNAL_ERROR"));
        }
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Listar usuarios (HU4.1)", 
        description = "Obtiene la lista completa de usuarios del sistema con información de roles y estado"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de usuarios obtenida exitosamente",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "No tiene permisos de administrador"
        )
    })
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        try {
            List<User> users = userQueryService.findAll();
            List<UserDTO> userDTOs = users.stream()
                .map(userTransformer::toDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(
                ApiResponse.success("Usuarios obtenidos exitosamente", "USERS_FOUND", userDTOs)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al obtener usuarios", "QUERY_ERROR"));
        }
    }
    
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener usuario por ID", 
        description = "Obtiene la información detallada de un usuario específico"
    )
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long userId) {
        return userQueryService.findById(userId)
            .map(user -> {
                UserDTO userDTO = userTransformer.toDTO(user);
                return ResponseEntity.ok(
                    ApiResponse.success("Usuario encontrado", "USER_FOUND", userDTO)
                );
            })
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Usuario no encontrado", "USER_NOT_FOUND")));
    }
    
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Actualizar usuario (HU4.1)", 
        description = "Actualiza la información de un usuario existente incluyendo datos personales y estado"
    )
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long userId, 
            @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest) {
        try {
            Optional<User> userOpt = userQueryService.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", "USER_NOT_FOUND"));
            }
            
            User user = userOpt.get();
            String oldValues = String.format("firstName: %s, lastName: %s, active: %s", 
                user.getFirstName(), user.getLastName(), user.isActive());
            
            // Actualizar información personal
            user.updatePersonalInfo(request.getFirstName(), request.getLastName());
            
            // Actualizar estado activo/inactivo
            if (request.isActive() != user.isActive()) {
                if (request.isActive()) {
                    user.activate();
                } else {
                    user.deactivate();
                }
            }
            
            userRepository.save(user);
            
            String newValues = String.format("firstName: %s, lastName: %s, active: %s", 
                user.getFirstName(), user.getLastName(), user.isActive());
            
            // Auditoría comentada temporalmente para debuggear
            /*
            auditCommandService.logUserModification(
                getCurrentUserId(), getCurrentUsername(), userId, 
                user.getUsername().toString(), oldValues, newValues, 
                getClientIpAddress(httpRequest)
            );
            */
            
            UserDTO userDTO = userTransformer.toDTO(user);
            return ResponseEntity.ok(
                ApiResponse.success("Usuario actualizado exitosamente", "USER_UPDATED", userDTO)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al actualizar usuario", "UPDATE_ERROR"));
        }
    }
    
    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Asignar roles a usuario (HU4.2)", 
        description = "Asigna o modifica los roles de un usuario para controlar el acceso a funcionalidades específicas"
    )
    public ResponseEntity<ApiResponse<UserDTO>> assignRoles(
            @PathVariable Long userId, 
            @RequestBody AssignRolesRequest request,
            HttpServletRequest httpRequest) {
        try {
            Optional<User> userOpt = userQueryService.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", "USER_NOT_FOUND"));
            }
            
            User user = userOpt.get();
            List<Role> newRoles = roleRepository.findByNameIn(request.getRoleNames());
            
            if (newRoles.size() != request.getRoleNames().size()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Uno o más roles especificados no existen", "INVALID_ROLES"));
            }
            
            Set<String> oldRoleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
            
            // Limpiar roles actuales y asignar nuevos
            user.getRoles().clear();
            newRoles.forEach(user::addRole);
            
            userRepository.save(user);
            
            // Auditoría
            String oldValues = "Roles anteriores: " + String.join(", ", oldRoleNames);
            String newValues = "Roles actuales: " + String.join(", ", request.getRoleNames());
            auditCommandService.logUserModification(
                getCurrentUserId(), getCurrentUsername(), userId, 
                user.getUsername().toString(), oldValues, newValues, 
                getClientIpAddress(httpRequest)
            );
            
            UserDTO userDTO = userTransformer.toDTO(user);
            return ResponseEntity.ok(
                ApiResponse.success("Roles asignados exitosamente", "ROLES_ASSIGNED", userDTO)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al asignar roles", "ROLE_ASSIGNMENT_ERROR"));
        }
    }
    
    @PutMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Activar usuario (HU4.1)", 
        description = "Activa una cuenta de usuario previamente desactivada"
    )
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long userId, HttpServletRequest httpRequest) {
        return toggleUserStatus(userId, true, httpRequest);
    }
    
    @PutMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Desactivar usuario (HU4.1)", 
        description = "Desactiva una cuenta de usuario para bloquear el acceso al sistema"
    )
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long userId, HttpServletRequest httpRequest) {
        return toggleUserStatus(userId, false, httpRequest);
    }
    
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Eliminar usuario (HU4.1)", 
        description = "Elimina permanentemente una cuenta de usuario del sistema"
    )
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId, HttpServletRequest httpRequest) {
        try {
            Optional<User> userOpt = userQueryService.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", "USER_NOT_FOUND"));
            }
            
            User user = userOpt.get();
            String username = user.getUsername().toString();
            
            userRepository.delete(user);
            
            // Auditoría
            auditCommandService.logAction(
                getCurrentUserId(), getCurrentUsername(), "USER_DELETE", "USER", userId,
                "Usuario: " + username, null, getClientIpAddress(httpRequest), null,
                com.las4as.POSBackend.IAM.Domain.model.entities.AuditLog.AuditSeverity.HIGH,
                "Usuario eliminado permanentemente"
            );
            
            return ResponseEntity.ok(
                ApiResponse.success("Usuario eliminado exitosamente", "USER_DELETED", null)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al eliminar usuario", "DELETE_ERROR"));
        }
    }
    
    private ResponseEntity<ApiResponse<Void>> toggleUserStatus(Long userId, boolean activate, HttpServletRequest httpRequest) {
        try {
            Optional<User> userOpt = userQueryService.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Usuario no encontrado", "USER_NOT_FOUND"));
            }
            
            User user = userOpt.get();
            boolean wasActive = user.isActive();
            
            if (activate) {
                user.activate();
            } else {
                user.deactivate();
            }
            
            userRepository.save(user);
            
            // Auditoría
            String action = activate ? "USER_ACTIVATE" : "USER_DEACTIVATE";
            String description = activate ? "Usuario activado" : "Usuario desactivado";
            auditCommandService.logAction(
                getCurrentUserId(), getCurrentUsername(), action, "USER", userId,
                "Activo: " + wasActive, "Activo: " + activate, 
                getClientIpAddress(httpRequest), null,
                com.las4as.POSBackend.IAM.Domain.model.entities.AuditLog.AuditSeverity.MEDIUM,
                description
            );
            
            String message = activate ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente";
            String code = activate ? "USER_ACTIVATED" : "USER_DEACTIVATED";
            
            return ResponseEntity.ok(ApiResponse.success(message, code, null));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al cambiar estado del usuario", "STATUS_CHANGE_ERROR"));
        }
    }
    
    // Métodos auxiliares (implementar según el contexto de seguridad)
    private Long getCurrentUserId() {
        return SecurityContextUtils.getCurrentUserId();
    }
    
    private String getCurrentUsername() {
        return SecurityContextUtils.getCurrentUsername();
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    // DTOs
    @Schema(description = "Request para crear un nuevo usuario")
    public static class CreateUserRequest {
        @Schema(description = "Nombre de usuario único", example = "mcajero", required = true)
        private String username;
        
        @Schema(description = "Email único del usuario", example = "maria.cajero@pos.com", required = true)
        private String email;
        
        @Schema(description = "Contraseña que cumple políticas de seguridad", example = "Secure123!@#", required = true)
        private String password;
        
        @Schema(description = "Nombres del usuario", example = "María", required = true)
        private String firstName;
        
        @Schema(description = "Apellidos del usuario", example = "Cajero", required = true)
        private String lastName;
        
        @Schema(description = "Lista de roles a asignar", example = "[\"CASHIER\"]", required = true)
        private List<String> roleNames;
        
        // Getters y setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public List<String> getRoleNames() { return roleNames; }
        public void setRoleNames(List<String> roleNames) { this.roleNames = roleNames; }
    }
    
    @Schema(description = "Request para actualizar un usuario")
    public static class UpdateUserRequest {
        @Schema(description = "Nombres del usuario", example = "María Actualizada")
        private String firstName;
        
        @Schema(description = "Apellidos del usuario", example = "Cajero Principal")
        private String lastName;
        
        @Schema(description = "Estado activo del usuario", example = "true")
        private Boolean active;
        
        // Getters y setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
        
        // Método helper para compatibilidad
        public Boolean isActive() { return active; }
    }
    
    @Schema(description = "Request para asignar roles a un usuario")
    public static class AssignRolesRequest {
        @Schema(description = "Lista de nombres de roles", example = "[\"CASHIER\", \"INVENTORY_VIEWER\"]", required = true)
        private List<String> roleNames;
        
        // Getters y setters
        public List<String> getRoleNames() { return roleNames; }
        public void setRoleNames(List<String> roleNames) { this.roleNames = roleNames; }
    }
}
