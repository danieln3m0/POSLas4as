package com.las4as.POSBackend.IAM.Interfaces.resources;

import com.las4as.POSBackend.IAM.Application.queryServices.UserQueryService;
import com.las4as.POSBackend.IAM.Application.outboundServices.TokenService;
import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Password;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Username;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y retorna un token JWT")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            Username username = new Username(request.getUsername());
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
    @Operation(summary = "Validar token", description = "Valida un token JWT y retorna la información del usuario")
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
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class TokenRequest {
        private String token;
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }
} 