package com.las4as.POSBackend.IAM.Interfaces.resources;

import com.las4as.POSBackend.IAM.Application.outboundServices.TokenService;
import com.las4as.POSBackend.IAM.Application.queryServices.UserQueryService;
import com.las4as.POSBackend.IAM.Infrastructure.authorization.UserPrincipal;
import com.las4as.POSBackend.shared.interfaces.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación de usuarios (HU4.1)
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación y autorización de usuarios")
public class AuthenticationResource {
    
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserQueryService userQueryService;
    
    @PostMapping("/login")
    @Operation(
        summary = "Iniciar sesión (HU4.1)", 
        description = "Autentica a un usuario y devuelve un token JWT para acceso a los recursos protegidos"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Autenticación exitosa",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Login exitoso",
                    summary = "Respuesta exitosa de autenticación",
                    value = """
                    {
                      "success": true,
                      "message": "Autenticación exitosa",
                      "code": "LOGIN_SUCCESS",
                      "data": {
                        "token": "eyJhbGciOiJIUzUxMiJ9...",
                        "type": "Bearer",
                        "expiresIn": 86400
                      }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas"
        )
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest loginRequest,
                                                          HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // Cargar el User completo desde el service
            com.las4as.POSBackend.IAM.Domain.model.aggregates.User user = 
                userQueryService.findByUsername(userPrincipal.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            String jwt = tokenService.generateToken(user);
            
            AuthResponse authResponse = new AuthResponse(jwt, "Bearer", 86400); // 24 horas
            
            return ResponseEntity.ok(
                ApiResponse.success("Autenticación exitosa", "LOGIN_SUCCESS", authResponse)
            );
            
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Credenciales inválidas", "INVALID_CREDENTIALS"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Error de autenticación", "AUTH_ERROR"));
        }
    }
    
    @PostMapping("/refresh")
    @Operation(
        summary = "Refrescar token JWT", 
        description = "Renueva un token JWT válido antes de su expiración"
    )
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                
                // Validar el token y obtener el username
                String username = tokenService.validateToken(jwt);
                
                if (username != null) {
                    // Cargar el usuario y generar nuevo token
                    com.las4as.POSBackend.IAM.Domain.model.aggregates.User user = 
                        userQueryService.findByUsername(username)
                            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                    
                    String newJwt = tokenService.generateToken(user);
                    
                    AuthResponse authResponse = new AuthResponse(newJwt, "Bearer", 86400);
                    
                    return ResponseEntity.ok(
                        ApiResponse.success("Token renovado exitosamente", "TOKEN_REFRESHED", authResponse)
                    );
                }
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Token inválido", "INVALID_TOKEN"));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error al renovar token", "REFRESH_ERROR"));
        }
    }
    
    // DTOs
    @Schema(description = "Request de login")
    public static class LoginRequest {
        @Schema(description = "Nombre de usuario", example = "admin", required = true)
        private String username;
        
        @Schema(description = "Contraseña", example = "password123", required = true)
        private String password;
        
        // Getters y setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    @Schema(description = "Response de autenticación")
    public static class AuthResponse {
        @Schema(description = "Token JWT", example = "eyJhbGciOiJIUzUxMiJ9...")
        private String token;
        
        @Schema(description = "Tipo de token", example = "Bearer")
        private String type;
        
        @Schema(description = "Tiempo de expiración en segundos", example = "86400")
        private int expiresIn;
        
        public AuthResponse(String token, String type, int expiresIn) {
            this.token = token;
            this.type = type;
            this.expiresIn = expiresIn;
        }
        
        // Getters y setters
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public int getExpiresIn() { return expiresIn; }
        public void setExpiresIn(int expiresIn) { this.expiresIn = expiresIn; }
    }
}
