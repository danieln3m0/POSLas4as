package com.las4as.POSBackend.Inventory.Domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductStockUpdatedEvent extends ApplicationEvent {
    private final Long productId;
    private final String sku;
    private final String name;
    private final int newStock;
    private final int previousStock;
    private final String locationName;
    
    public ProductStockUpdatedEvent(Long productId, String sku, String name, 
                                  int newStock, int previousStock, String locationName) {
        super(productId);
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.newStock = newStock;
        this.previousStock = previousStock;
        this.locationName = locationName;
    }
} 