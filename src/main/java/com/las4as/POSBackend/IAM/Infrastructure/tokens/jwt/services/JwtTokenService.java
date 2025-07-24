package com.las4as.POSBackend.IAM.Infrastructure.tokens.jwt.services;

import com.las4as.POSBackend.IAM.Application.outboundServices.TokenService;
import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtTokenService implements TokenService {
    
    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnly}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}")
    private long expiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    @Override
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail().toString());
        claims.put("roles", user.getRoles().stream().map(role -> role.getName()).toArray());
        
        Instant now = Instant.now();
        Instant expirationTime = now.plusMillis(expiration);
        
        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expirationTime))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }
    
    @Override
    public String validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Token inválido");
        }
    }
    
    @Override
    public String refreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            // Crear nuevo token con la misma información pero nueva expiración
            Instant now = Instant.now();
            Instant expirationTime = now.plusMillis(expiration);
            
            return Jwts.builder()
                    .claims(claims)
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(expirationTime))
                    .signWith(getSigningKey(), Jwts.SIG.HS256)
                    .compact();
        } catch (JwtException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Token inválido para refresh");
        }
    }
} 