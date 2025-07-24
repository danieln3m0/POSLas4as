package com.las4as.POSBackend.Inventory.Domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductCreatedEvent extends ApplicationEvent {
    private final Long productId;
    private final String sku;
    private final String name;
    
    public ProductCreatedEvent(Long productId, String sku, String name) {
        super(productId);
        this.productId = productId;
        this.sku = sku;
        this.name = name;
    }
} 