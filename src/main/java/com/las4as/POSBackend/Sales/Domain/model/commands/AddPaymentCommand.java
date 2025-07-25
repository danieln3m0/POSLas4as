package com.las4as.POSBackend.Sales.Domain.model.commands;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class AddPaymentCommand {
    private final Long saleId;
    private final String paymentMethod;
    private final BigDecimal amount;
    private final String referenceNumber;
    private final String notes;
    
    public AddPaymentCommand(Long saleId, String paymentMethod, BigDecimal amount, 
                           String referenceNumber, String notes) {
        this.saleId = saleId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
    }
}
