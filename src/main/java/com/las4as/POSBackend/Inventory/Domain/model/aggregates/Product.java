package com.las4as.POSBackend.Inventory.Domain.model.aggregates;

import com.las4as.POSBackend.Inventory.Domain.model.entities.Category;
import com.las4as.POSBackend.Inventory.Domain.model.entities.Supplier;
import com.las4as.POSBackend.Inventory.Domain.model.entities.StockItem;
import com.las4as.POSBackend.Inventory.Domain.model.events.ProductCreatedEvent;
import com.las4as.POSBackend.Inventory.Domain.model.valueobjects.Price;
import com.las4as.POSBackend.Inventory.Domain.model.valueobjects.SKU;
import com.las4as.POSBackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.math.RoundingMode;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
public class Product extends AuditableAbstractAggregateRoot<Product> {
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "sku", nullable = false, unique = true, length = 50))
    })
    private SKU sku;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "purchase_price", nullable = false, precision = 10, scale = 2))
    })
    private Price purchasePrice;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "sale_price", nullable = false, precision = 10, scale = 2))
    })
    private Price salePrice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
    
    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure;
    
    @Column(name = "barcode", length = 100)
    private String barcode;
    
    @Column(name = "qr_code", length = 500)
    private String qrCode;
    
    @Column(name = "minimum_stock", nullable = false)
    private int minimumStock = 0;
    
    @Column(name = "maximum_stock")
    private Integer maximumStock;
    
    @Column(name = "reorder_point", nullable = false)
    private int reorderPoint = 0;
    
    @Column(name = "lead_time_days")
    private Integer leadTimeDays;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StockItem> stockItems = new ArrayList<>();
    
    public Product(SKU sku, String name, String description, Price purchasePrice, 
                  Price salePrice, Category category, Supplier supplier, String unitOfMeasure,
                  String barcode, String qrCode, int minimumStock, Integer maximumStock,
                  int reorderPoint, Integer leadTimeDays) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.category = category;
        this.supplier = supplier;
        this.unitOfMeasure = unitOfMeasure;
        this.barcode = barcode;
        this.qrCode = qrCode;
        this.minimumStock = minimumStock;
        this.maximumStock = maximumStock;
        this.reorderPoint = reorderPoint;
        this.leadTimeDays = leadTimeDays;
        
        // Publicar evento de dominio
        registerEvent(new ProductCreatedEvent(this, null, this.sku.toString(), this.name));
    }
    
    public void updateBasicInfo(String name, String description, Price purchasePrice, Price salePrice) {
        this.name = name;
        this.description = description;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
    }
    
    public void updateStockSettings(int minimumStock, Integer maximumStock, int reorderPoint, Integer leadTimeDays) {
        this.minimumStock = minimumStock;
        this.maximumStock = maximumStock;
        this.reorderPoint = reorderPoint;
        this.leadTimeDays = leadTimeDays;
    }
    
    public void updateCodes(String barcode, String qrCode) {
        this.barcode = barcode;
        this.qrCode = qrCode;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void addStockItem(StockItem stockItem) {
        this.stockItems.add(stockItem);
        stockItem.setProduct(this);
    }
    
    public int getTotalStock() {
        return this.stockItems.stream()
                .mapToInt(item -> item.getQuantity().getValue())
                .sum();
    }
    
    public boolean isLowStock() {
        return getTotalStock() <= minimumStock;
    }
    
    public boolean needsReorder() {
        return getTotalStock() <= reorderPoint;
    }
    
    public boolean isOutOfStock() {
        return getTotalStock() == 0;
    }
    
    public double getProfitMargin() {
        if (purchasePrice.getValue().compareTo(java.math.BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        return salePrice.getValue()
                .subtract(purchasePrice.getValue())
                .divide(purchasePrice.getValue(), 4, RoundingMode.HALF_UP)
                .multiply(java.math.BigDecimal.valueOf(100))
                .doubleValue();
    }
    
    public void registerDomainEvent(Object event) {
        registerEvent(event);
    }
} 