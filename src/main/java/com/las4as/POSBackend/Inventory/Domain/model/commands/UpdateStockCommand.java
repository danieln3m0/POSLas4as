package com.las4as.POSBackend.Inventory.Domain.model.commands;

import lombok.Getter;

@Getter
public class UpdateStockCommand {
    private final Long productId;
    private final Long locationId;
    private final int quantity;
    private final String batchNumber;
    private final String expirationDate;
    private final String lotNumber;
    private final String notes;
    private final String operationType; // "ADD", "SUBTRACT", "SET"
    
    public UpdateStockCommand(Long productId, Long locationId, int quantity, 
                            String batchNumber, String expirationDate, 
                            String lotNumber, String notes, String operationType) {
        this.productId = productId;
        this.locationId = locationId;
        this.quantity = quantity;
        this.batchNumber = batchNumber;
        this.expirationDate = expirationDate;
        this.lotNumber = lotNumber;
        this.notes = notes;
        this.operationType = operationType;
    }
} 