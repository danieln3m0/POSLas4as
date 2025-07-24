package com.las4as.POSBackend.Inventory.Domain.model.entities;

import com.las4as.POSBackend.Inventory.Domain.model.aggregates.Product;
import com.las4as.POSBackend.Inventory.Domain.model.valueobjects.Quantity;
import com.las4as.POSBackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_items")
@Getter
@NoArgsConstructor
public class StockItem extends AuditableModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @Setter
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "quantity", nullable = false))
    })
    private Quantity quantity;
    
    @Column(name = "batch_number", length = 100)
    private String batchNumber;
    
    @Column(name = "expiration_date")
    private java.time.LocalDate expirationDate;
    
    @Column(name = "lot_number", length = 100)
    private String lotNumber;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    public StockItem(Product product, Location location, Quantity quantity, 
                    String batchNumber, java.time.LocalDate expirationDate, 
                    String lotNumber, String notes) {
        this.product = product;
        this.location = location;
        this.quantity = quantity;
        this.batchNumber = batchNumber;
        this.expirationDate = expirationDate;
        this.lotNumber = lotNumber;
        this.notes = notes;
    }
    
    public void addQuantity(Quantity quantityToAdd) {
        this.quantity = this.quantity.add(quantityToAdd);
    }
    
    public void subtractQuantity(Quantity quantityToSubtract) {
        this.quantity = this.quantity.subtract(quantityToSubtract);
    }
    
    public boolean hasExpired() {
        if (expirationDate == null) {
            return false;
        }
        return expirationDate.isBefore(java.time.LocalDate.now());
    }
    
    public boolean isExpiringSoon(int daysThreshold) {
        if (expirationDate == null) {
            return false;
        }
        java.time.LocalDate thresholdDate = java.time.LocalDate.now().plusDays(daysThreshold);
        return expirationDate.isBefore(thresholdDate) && !hasExpired();
    }
    
    public int getDaysUntilExpiration() {
        if (expirationDate == null) {
            return Integer.MAX_VALUE;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(
            java.time.LocalDate.now(), expirationDate);
    }
} 