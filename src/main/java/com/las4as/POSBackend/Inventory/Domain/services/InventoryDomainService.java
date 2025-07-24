package com.las4as.POSBackend.Inventory.Domain.services;

import com.las4as.POSBackend.Inventory.Domain.model.aggregates.Product;
import com.las4as.POSBackend.Inventory.Domain.model.commands.CreateProductCommand;
import com.las4as.POSBackend.Inventory.Domain.model.commands.UpdateStockCommand;
import com.las4as.POSBackend.Inventory.Domain.model.entities.Category;
import com.las4as.POSBackend.Inventory.Domain.model.entities.Location;
import com.las4as.POSBackend.Inventory.Domain.model.entities.StockItem;
import com.las4as.POSBackend.Inventory.Domain.model.entities.Supplier;
import com.las4as.POSBackend.Inventory.Domain.model.valueobjects.Quantity;
import com.las4as.POSBackend.Inventory.Domain.model.valueobjects.SKU;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InventoryDomainService {
    
    /**
     * Crea un nuevo producto
     */
    Product createProduct(CreateProductCommand command, Category category, Supplier supplier);
    
    /**
     * Actualiza el stock de un producto en una ubicación específica
     */
    void updateStock(Product product, Location location, UpdateStockCommand command);
    
    /**
     * Transfiere stock entre ubicaciones
     */
    void transferStock(Product product, Location fromLocation, Location toLocation, Quantity quantity);
    
    /**
     * Verifica si un SKU ya existe
     */
    boolean isSkuTaken(SKU sku);
    
    /**
     * Verifica si un código de barras ya existe
     */
    boolean isBarcodeTaken(String barcode);
    
    /**
     * Obtiene el stock total de un producto
     */
    int getTotalStock(Product product);
    
    /**
     * Obtiene el stock de un producto en una ubicación específica
     */
    int getStockAtLocation(Product product, Location location);
    
    /**
     * Verifica si un producto tiene stock bajo
     */
    boolean isLowStock(Product product);
    
    /**
     * Verifica si un producto necesita reorden
     */
    boolean needsReorder(Product product);
    
    /**
     * Calcula la cantidad sugerida para reorden
     */
    int calculateReorderQuantity(Product product);
    
    /**
     * Obtiene productos con stock bajo
     */
    List<Product> getLowStockProducts();
    
    /**
     * Obtiene productos que necesitan reorden
     */
    List<Product> getProductsNeedingReorder();
    
    /**
     * Obtiene productos próximos a expirar
     */
    List<StockItem> getExpiringStockItems(int daysThreshold);
} 