package com.las4as.POSBackend.Inventory.Domain.model.valueobjects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Quantity {
    private int value;
    
    public Quantity(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("La cantidad no puede ser negativa");
        }
        
        this.value = value;
    }
    
    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }
    
    public Quantity subtract(Quantity other) {
        int result = this.value - other.value;
        if (result < 0) {
            throw new IllegalArgumentException("La cantidad resultante no puede ser negativa");
        }
        return new Quantity(result);
    }
    
    public boolean isZero() {
        return value == 0;
    }
    
    public boolean isLessThan(Quantity other) {
        return this.value < other.value;
    }
    
    public boolean isGreaterThan(Quantity other) {
        return this.value > other.value;
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
} 