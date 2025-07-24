package com.las4as.POSBackend.shared.interfaces.rest.resources;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeResource {
    
    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Bienvenido al Sistema de Gestión POS de A&S Soluciones Generales E.I.R.L");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("documentation", "/swagger-ui.html");
        response.put("endpoints", Map.of(
            "auth", "/api/v1/auth",
            "users", "/api/v1/users",
            "products", "/api/v1/products",
            "swagger", "/swagger-ui.html"
        ));
        return response;
    }
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
} 