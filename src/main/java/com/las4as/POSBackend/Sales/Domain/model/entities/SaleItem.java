package com.las4as.POSBackend.Sales.Domain.model.entities;

import com.las4as.POSBackend.Inventory.Domain.model.aggregates.Product;
import com.las4as.POSBackend.Sales.Domain.model.valueobjects.Discount;
import com.las4as.POSBackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "sale_items")
@Getter
@NoArgsConstructor
public class SaleItem extends AuditableModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "quantity", nullable = false)
    private int quantity;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "percentage", column = @Column(name = "discount_percentage", precision = 5, scale = 2)),
        @AttributeOverride(name = "amount", column = @Column(name = "discount_amount", precision = 10, scale = 2)),
        @AttributeOverride(name = "type", column = @Column(name = "discount_type", length = 20))
    })
    private Discount discount;
    
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(name = "notes", length = 200)
    private String notes;
    
    public SaleItem(Product product, int quantity, BigDecimal unitPrice, Discount discount, String notes) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
        this.discount = discount != null ? discount : Discount.noDiscount();
        this.notes = notes;
        
        calculateSubtotal();
    }
    
    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        this.quantity = newQuantity;
        calculateSubtotal();
    }
    
    public void updateDiscount(Discount newDiscount) {
        this.discount = newDiscount != null ? newDiscount : Discount.noDiscount();
        calculateSubtotal();
    }
    
    private void calculateSubtotal() {
        BigDecimal baseAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal discountAmount = discount.calculateDiscount(baseAmount);
        this.subtotal = baseAmount.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal getTotalDiscount() {
        BigDecimal baseAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return discount.calculateDiscount(baseAmount);
    }
    
    public BigDecimal getBaseAmount() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
