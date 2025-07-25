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
public class Discount {
    private BigDecimal percentage;
    private BigDecimal amount;
    private String type; // PERCENTAGE, FIXED_AMOUNT
    
    public Discount(BigDecimal percentage) {
        if (percentage == null || percentage.compareTo(BigDecimal.ZERO) < 0 || percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100");
        }
        this.percentage = percentage.setScale(2, RoundingMode.HALF_UP);
        this.amount = BigDecimal.ZERO;
        this.type = "PERCENTAGE";
    }
    
    public Discount(BigDecimal amount, String type) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto de descuento no puede ser negativo");
        }
        if (!"FIXED_AMOUNT".equals(type)) {
            throw new IllegalArgumentException("Tipo de descuento inválido");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.percentage = BigDecimal.ZERO;
        this.type = type;
    }
    
    public BigDecimal calculateDiscount(BigDecimal baseAmount) {
        if ("PERCENTAGE".equals(type)) {
            return baseAmount.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            return amount.min(baseAmount); // No puede ser mayor que el monto base
        }
    }
    
    public boolean isPercentage() {
        return "PERCENTAGE".equals(type);
    }
    
    public boolean isFixedAmount() {
        return "FIXED_AMOUNT".equals(type);
    }
    
    public static Discount noDiscount() {
        return new Discount(BigDecimal.ZERO);
    }
}
