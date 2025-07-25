package com.las4as.POSBackend.Sales.Domain.model.commands;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class CreateSaleCommand {
    private final Long customerId;
    private final Long cashierId;
    private final List<SaleItemData> items;
    private final String notes;
    
    public CreateSaleCommand(Long customerId, Long cashierId, List<SaleItemData> items, String notes) {
        this.customerId = customerId;
        this.cashierId = cashierId;
        this.items = items;
        this.notes = notes;
    }
    
    @Getter
    public static class SaleItemData {
        private final Long productId;
        private final int quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal discountPercentage;
        private final BigDecimal discountAmount;
        private final String discountType;
        private final String notes;
        
        public SaleItemData(Long productId, int quantity, BigDecimal unitPrice, 
                           BigDecimal discountPercentage, BigDecimal discountAmount, 
                           String discountType, String notes) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.discountPercentage = discountPercentage;
            this.discountAmount = discountAmount;
            this.discountType = discountType;
            this.notes = notes;
        }
    }
}
