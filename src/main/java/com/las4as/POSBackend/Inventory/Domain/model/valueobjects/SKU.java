package com.las4as.POSBackend.Inventory.Domain.model.valueobjects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class SKU {
    private String value;
    
    public SKU(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El SKU no puede estar vacío");
        }
        
        if (value.length() < 3 || value.length() > 50) {
            throw new IllegalArgumentException("El SKU debe tener entre 3 y 50 caracteres");
        }
        
        // Solo permitir letras, números y guiones
        if (!value.matches("^[A-Z0-9-]+$")) {
            throw new IllegalArgumentException("El SKU solo puede contener letras mayúsculas, números y guiones");
        }
        
        this.value = value.toUpperCase().trim();
    }
    
    @Override
    public String toString() {
        return value;
    }
} 