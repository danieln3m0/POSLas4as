package com.las4as.POSBackend.Customers.Domain.model.valueobjects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class DocumentNumber {
    private String value;
    
    public DocumentNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de documento no puede estar vacío");
        }
        
        // Validación básica para DNI (8 dígitos) y RUC (11 dígitos)
        String cleanValue = value.trim().replaceAll("[^0-9]", "");
        if (cleanValue.length() != 8 && cleanValue.length() != 11) {
            throw new IllegalArgumentException("El número de documento debe tener 8 dígitos (DNI) o 11 dígitos (RUC)");
        }
        
        this.value = cleanValue;
    }
    
    public boolean isDNI() {
        return value.length() == 8;
    }
    
    public boolean isRUC() {
        return value.length() == 11;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DocumentNumber that = (DocumentNumber) obj;
        return value.equals(that.value);
    }
    
    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
