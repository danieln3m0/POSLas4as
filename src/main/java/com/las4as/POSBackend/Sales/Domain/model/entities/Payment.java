package com.las4as.POSBackend.Sales.Domain.model.entities;

import com.las4as.POSBackend.Sales.Domain.model.valueobjects.PaymentAmount;
import com.las4as.POSBackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class Payment extends AuditableModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "payment_method", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "amount", nullable = false, precision = 10, scale = 2))
    })
    private PaymentAmount amount;
    
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;
    
    @Column(name = "notes", length = 200)
    private String notes;
    
    public enum PaymentMethod {
        CASH("Efectivo"),
        CREDIT_CARD("Tarjeta de Crédito"),
        DEBIT_CARD("Tarjeta de Débito"),
        DIGITAL_PAYMENT("Pago Digital"),
        BANK_TRANSFER("Transferencia Bancaria"),
        CHECK("Cheque");
        
        private final String displayName;
        
        PaymentMethod(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public Payment(PaymentMethod paymentMethod, PaymentAmount amount, String referenceNumber, String notes) {
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
    }
    
    public boolean isCash() {
        return paymentMethod == PaymentMethod.CASH;
    }
    
    public boolean isCard() {
        return paymentMethod == PaymentMethod.CREDIT_CARD || paymentMethod == PaymentMethod.DEBIT_CARD;
    }
    
    public boolean isDigital() {
        return paymentMethod == PaymentMethod.DIGITAL_PAYMENT;
    }
}
