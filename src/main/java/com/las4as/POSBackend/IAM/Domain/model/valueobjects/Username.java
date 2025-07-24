package com.las4as.POSBackend.IAM.Domain.model.valueobjects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Username {
    private String value;
    
    public Username(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
        }
        
        if (value.length() < 3 || value.length() > 50) {
            throw new IllegalArgumentException("El nombre de usuario debe tener entre 3 y 50 caracteres");
        }
        
        // Solo permitir letras, números y guiones bajos
        if (!value.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("El nombre de usuario solo puede contener letras, números y guiones bajos");
        }
        
        this.value = value.toLowerCase().trim();
    }
    
    @Override
    public String toString() {
        return value;
    }
} 