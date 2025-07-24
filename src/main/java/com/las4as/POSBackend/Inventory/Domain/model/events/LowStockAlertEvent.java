package com.las4as.POSBackend.Inventory.Domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LowStockAlertEvent extends ApplicationEvent {
    private final Long productId;
    private final String sku;
    private final String name;
    private final int currentStock;
    private final int minimumStock;
    private final String locationName;
    
    public LowStockAlertEvent(Long productId, String sku, String name, 
                            int currentStock, int minimumStock, String locationName) {
        super(productId);
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.currentStock = currentStock;
        this.minimumStock = minimumStock;
        this.locationName = locationName;
    }
} 