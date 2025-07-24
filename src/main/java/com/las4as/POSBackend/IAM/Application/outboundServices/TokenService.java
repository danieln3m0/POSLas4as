package com.las4as.POSBackend.IAM.Application.outboundServices;

import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;

public interface TokenService {
    
    /**
     * Genera un token JWT para un usuario
     */
    String generateToken(User user);
    
    /**
     * Valida un token JWT y retorna el username
     */
    String validateToken(String token);
    
    /**
     * Refresca un token JWT
     */
    String refreshToken(String token);
} 