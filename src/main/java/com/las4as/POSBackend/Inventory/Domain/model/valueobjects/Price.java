package com.las4as.POSBackend.Inventory.Domain.model.valueobjects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Price {
    private BigDecimal value;
    
    public Price(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("El precio no puede ser nulo");
        }
        
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        
        this.value = value.setScale(2, RoundingMode.HALF_UP);
    }
    
    public Price(String value) {
        this(new BigDecimal(value));
    }
    
    public Price add(Price other) {
        return new Price(this.value.add(other.value));
    }
    
    public Price subtract(Price other) {
        return new Price(this.value.subtract(other.value));
    }
    
    public Price multiply(int quantity) {
        return new Price(this.value.multiply(BigDecimal.valueOf(quantity)));
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
} 