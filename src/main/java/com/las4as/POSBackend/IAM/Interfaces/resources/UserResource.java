package com.las4as.POSBackend.IAM.Interfaces.resources;

import com.las4as.POSBackend.IAM.Application.commandServices.CreateUserCommandService;
import com.las4as.POSBackend.IAM.Application.queryServices.UserQueryService;
import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import com.las4as.POSBackend.IAM.Domain.model.commands.CreateUserCommand;
import com.las4as.POSBackend.IAM.Interfaces.transform.UserDTO;
import com.las4as.POSBackend.IAM.Interfaces.transform.UserTransformer;
import com.las4as.POSBackend.shared.interfaces.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Gestión de Usuarios", description = "Endpoints para la gestión de usuarios del sistema")
public class UserResource {
    
    private final CreateUserCommandService createUserCommandService;
    private final UserQueryService userQueryService;
    private final UserTransformer userTransformer;
    
    @PostMapping
    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario en el sistema")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@RequestBody CreateUserRequest request) {
        try {
            // Validar que los campos requeridos no estén vacíos
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("El nombre de usuario es requerido", "USERNAME_REQUIRED"));
            }
            
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("El email es requerido", "EMAIL_REQUIRED"));
            }
            
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("La contraseña es requerida", "PASSWORD_REQUIRED"));
            }
            
            if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("El nombre es requerido", "FIRST_NAME_REQUIRED"));
            }
            
            if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("El apellido es requerido", "LAST_NAME_REQUIRED"));
            }
            
            // Verificar si el username ya existe
            if (userQueryService.existsByUsername(request.getUsername().trim())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("El nombre de usuario ya está en uso", "USERNAME_ALREADY_EXISTS"));
            }
            
            // Verificar si el email ya existe
            if (userQueryService.existsByEmail(request.getEmail().trim())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error("El email ya está en uso", "EMAIL_ALREADY_EXISTS"));
            }

            // Validar roles (si no se especifican, asignar rol USER por defecto)
            List<String> roleNames = request.getRoleNames();
            if (roleNames == null || roleNames.isEmpty()) {
                roleNames = List.of("USER");
            }

            CreateUserCommand command = new CreateUserCommand(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                roleNames
            );
            
            User user = createUserCommandService.execute(command);
            UserDTO userDTO = userTransformer.toDTO(user);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Usuario creado exitosamente", "USER_CREATED", userDTO));
                    
        } catch (IllegalArgumentException e) {
            // Identificar el tipo de error específico
            String errorMessage = e.getMessage();
            String errorCode = "VALIDATION_ERROR";
            HttpStatus status = HttpStatus.BAD_REQUEST;
            
            if (errorMessage.contains("username") || errorMessage.contains("nombre de usuario")) {
                errorCode = "INVALID_USERNAME";
            } else if (errorMessage.contains("email")) {
                errorCode = "INVALID_EMAIL";
            } else if (errorMessage.contains("password") || errorMessage.contains("contraseña")) {
                errorCode = "INVALID_PASSWORD";
            } else if (errorMessage.contains("nombre")) {
                errorCode = "INVALID_NAME";
            } else if (errorMessage.contains("ya está en uso") || errorMessage.contains("already exists")) {
                errorCode = "DUPLICATE_ENTRY";
                status = HttpStatus.CONFLICT;
            }
            
            return ResponseEntity.status(status)
                    .body(ApiResponse.error(errorMessage, errorCode));
                    
        } catch (RuntimeException e) {
            System.err.println("RuntimeException en createUser: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error interno del servidor. Por favor, intente nuevamente.", "INTERNAL_SERVER_ERROR"));
                    
        } catch (Exception e) {
            System.err.println("Exception en createUser: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error inesperado. Por favor, intente nuevamente.", "UNEXPECTED_ERROR"));
        }
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Obtener usuario por ID", description = "Obtiene la información de un usuario específico")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        return userQueryService.findById(userId)
                .map(user -> ResponseEntity.ok(userTransformer.toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Obtiene la lista de todos los usuarios activos")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userQueryService.findActiveUsers();
        List<UserDTO> userDTOs = users.stream()
                .map(userTransformer::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(userDTOs);
    }
    
    @GetMapping("/check-username/{username}")
    @Operation(summary = "Verificar disponibilidad de username", description = "Verifica si un nombre de usuario está disponible")
    public ResponseEntity<Boolean> isUsernameAvailable(@PathVariable String username) {
        boolean isAvailable = !userQueryService.existsByUsername(username);
        return ResponseEntity.ok(isAvailable);
    }
    
    @GetMapping("/check-email/{email}")
    @Operation(summary = "Verificar disponibilidad de email", description = "Verifica si un email está disponible")
    public ResponseEntity<Boolean> isEmailAvailable(@PathVariable String email) {
        boolean isAvailable = !userQueryService.existsByEmail(email);
        return ResponseEntity.ok(isAvailable);
    }
    
    // Clase interna para el request
    public static class CreateUserRequest {
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
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
} 