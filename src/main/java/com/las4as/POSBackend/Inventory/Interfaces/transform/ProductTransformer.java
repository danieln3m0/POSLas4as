package com.las4as.POSBackend.Inventory.Interfaces.transform;

import com.las4as.POSBackend.Inventory.Domain.model.aggregates.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductTransformer {
    
    public ProductDTO toDTO(Product product) {
        return ProductDTO.fromDomain(product);
    }
} 