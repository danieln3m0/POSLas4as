package com.las4as.POSBackend.IAM.Application.outboundServices;

public interface HashingService {
    
    /**
     * Genera un hash de un texto plano
     */
    String hash(String plainText);
    
    /**
     * Verifica si un texto plano coincide con un hash
     */
    boolean matches(String plainText, String hashedText);
} 