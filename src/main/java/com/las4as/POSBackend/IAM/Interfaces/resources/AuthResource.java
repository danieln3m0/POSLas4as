package com.las4as.POSBackend.IAM.Interfaces.resources;

import com.las4as.POSBackend.IAM.Application.queryServices.UserQueryService;
import com.las4as.POSBackend.IAM.Application.outboundServices.TokenService;
import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Password;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para la autenticación de usuarios")
public class AuthResource {
    
    private final UserQueryService userQueryService;
    private final TokenService tokenService;
    
    @PostMapping("/login")
    @Operation(
        summary = "Iniciar sesión", 
        description = "Autentica un usuario con sus credenciales y retorna un token JWT válido junto con la información del usuario",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Credenciales de acceso del usuario",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginRequest.class),
                examples = @ExampleObject(
                    name = "Ejemplo de login",
                    summary = "Credenciales de usuario administrador",
                    value = "{\n  \"username\": \"admin\",\n  \"password\": \"admin123\"\n}"
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Autenticación exitosa",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Login exitoso",
                    summary = "Respuesta con token y datos del usuario",
                    value = "{\n  \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n  \"user\": {\n    \"id\": 1,\n    \"username\": \"admin\",\n    \"email\": \"admin@pos.com\",\n    \"fullName\": \"Administrador Sistema\"\n  }\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Datos de entrada inválidos",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error de validación",
                    value = "{\n  \"error\": \"Datos de entrada inválidos\"\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Credenciales inválidas o usuario inactivo",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Credenciales incorrectas",
                    value = "{\n  \"error\": \"Credenciales inválidas\"\n}"
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            Password password = new Password(request.getPassword());
            
            // Buscar usuario por username
            User user = userQueryService.findByUsername(request.getUsername())
                    .orElse(null);
            
            if (user == null || !user.isActive()) {
                return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
            }
            
            // Validar contraseña (aquí deberías usar el servicio de hashing)
            if (!user.getPassword().toString().equals(password.toString())) {
                return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
            }
            
            // Generar token
            String token = tokenService.generateToken(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername().toString(),
                "email", user.getEmail().toString(),
                "fullName", user.getFullName()
            ));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/validate")
    @Operation(
        summary = "Validar token JWT", 
        description = "Valida un token JWT y retorna la información del usuario autenticado si el token es válido",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Token JWT a validar",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TokenRequest.class),
                examples = @ExampleObject(
                    name = "Ejemplo de validación",
                    summary = "Token JWT para validar",
                    value = "{\n  \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTY0MjY4NjAwMH0...\"\n}"
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Token válido",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Token válido",
                    summary = "Respuesta con validación exitosa",
                    value = "{\n  \"valid\": true,\n  \"user\": {\n    \"id\": 1,\n    \"username\": \"admin\",\n    \"email\": \"admin@pos.com\",\n    \"fullName\": \"Administrador Sistema\"\n  }\n}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Token inválido o expirado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Token inválido",
                    value = "{\n  \"error\": \"Token inválido\"\n}"
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody TokenRequest request) {
        try {
            String username = tokenService.validateToken(request.getToken());
            User user = userQueryService.findByUsername(username).orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername().toString(),
                "email", user.getEmail().toString(),
                "fullName", user.getFullName()
            ));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        }
    }
    
    // Clases internas para requests
    @Schema(description = "Datos requeridos para iniciar sesión")
    public static class LoginRequest {
        @Schema(description = "Nombre de usuario único en el sistema", example = "admin", required = true)
        private String username;
        
        @Schema(description = "Contraseña del usuario", example = "admin123", required = true)
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    @Schema(description = "Token JWT para validar")
    public static class TokenRequest {
        @Schema(description = "Token JWT generado en el login", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", required = true)
        private String token;
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
} 