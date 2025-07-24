package com.las4as.POSBackend.Inventory.Application.queryServices;

import com.las4as.POSBackend.Inventory.Domain.model.aggregates.Product;
import com.las4as.POSBackend.Inventory.Domain.services.InventoryDomainService;
import com.las4as.POSBackend.Inventory.Infrastructure.persistence.jpa.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryService {
    
    private final ProductRepository productRepository;
    private final InventoryDomainService inventoryDomainService;
    
    public Optional<Product> findById(Long productId) {
        return productRepository.findById(productId);
    }
    
    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySkuValue(sku);
    }
    
    public Optional<Product> findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode);
    }
    
    public List<Product> findAll() {
        return productRepository.findByIsActiveTrue();
    }
    
    public List<Product> findByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId);
    }
    
    public List<Product> findBySupplier(Long supplierId) {
        return productRepository.findBySupplierIdAndIsActiveTrue(supplierId);
    }
    
    public List<Product> searchProducts(String searchTerm) {
        return productRepository.searchProducts(searchTerm);
    }
    
    public List<Product> getLowStockProducts() {
        return inventoryDomainService.getLowStockProducts();
    }
    
    public List<Product> getProductsNeedingReorder() {
        return inventoryDomainService.getProductsNeedingReorder();
    }
    
    public int getTotalStock(Product product) {
        return inventoryDomainService.getTotalStock(product);
    }
    
    public boolean isLowStock(Product product) {
        return inventoryDomainService.isLowStock(product);
    }
    
    public boolean needsReorder(Product product) {
        return inventoryDomainService.needsReorder(product);
    }
    
    public int calculateReorderQuantity(Product product) {
        return inventoryDomainService.calculateReorderQuantity(product);
    }
} 