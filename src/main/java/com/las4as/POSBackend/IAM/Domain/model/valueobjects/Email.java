package com.las4as.POSBackend.IAM.Domain.model.valueobjects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Email {
    private String value;
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    public Email(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("El formato del email no es válido");
        }
        
        this.value = value.toLowerCase().trim();
    }
    
    @Override
    public String toString() {
        return value;
    }
} 