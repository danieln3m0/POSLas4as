package com.las4as.POSBackend.IAM.Domain.model.valueobjects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Password {
    private String value;
    
    public Password(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        
        if (value.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
        
        // Validar que contenga al menos una letra mayúscula, una minúscula y un número
        if (!value.matches(".*[A-Z].*") || !value.matches(".*[a-z].*") || !value.matches(".*\\d.*")) {
            throw new IllegalArgumentException("La contraseña debe contener al menos una letra mayúscula, una minúscula y un número");
        }
        
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value;
    }
} 