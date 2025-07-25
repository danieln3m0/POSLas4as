package com.las4as.POSBackend.Sales.Domain.model.valueobjects;

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
public class PaymentAmount {
    private BigDecimal value;
    
    public PaymentAmount(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("El monto de pago no puede ser nulo");
        }
        
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto de pago no puede ser negativo");
        }
        
        this.value = value.setScale(2, RoundingMode.HALF_UP);
    }
    
    public PaymentAmount(String value) {
        this(new BigDecimal(value));
    }
    
    public PaymentAmount add(PaymentAmount other) {
        return new PaymentAmount(this.value.add(other.value));
    }
    
    public PaymentAmount subtract(PaymentAmount other) {
        BigDecimal result = this.value.subtract(other.value);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto resultante no puede ser negativo");
        }
        return new PaymentAmount(result);
    }
    
    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }
    
    public boolean isGreaterThan(PaymentAmount other) {
        return value.compareTo(other.value) > 0;
    }
    
    public boolean isLessThan(PaymentAmount other) {
        return value.compareTo(other.value) < 0;
    }
    
    public boolean isEqualTo(PaymentAmount other) {
        return value.compareTo(other.value) == 0;
    }
    
    @Override
    public String toString() {
        return value.toString();
    }
}
