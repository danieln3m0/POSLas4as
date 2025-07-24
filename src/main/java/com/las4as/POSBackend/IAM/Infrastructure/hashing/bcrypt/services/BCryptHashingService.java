package com.las4as.POSBackend.IAM.Infrastructure.hashing.bcrypt.services;

import com.las4as.POSBackend.IAM.Application.outboundServices.HashingService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BCryptHashingService implements HashingService {
    
    private final BCryptPasswordEncoder passwordEncoder;
    
    public BCryptHashingService() {
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }
    
    @Override
    public String hash(String plainText) {
        return passwordEncoder.encode(plainText);
    }
    
    @Override
    public boolean matches(String plainText, String hashedText) {
        return passwordEncoder.matches(plainText, hashedText);
    }
} 