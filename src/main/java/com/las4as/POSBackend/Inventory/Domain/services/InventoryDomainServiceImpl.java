package com.las4as.POSBackend.Inventory.Domain.services;

import com.las4as.POSBackend.Inventory.Domain.model.aggregates.Product;
import com.las4as.POSBackend.Inventory.Domain.model.commands.CreateProductCommand;
import com.las4as.POSBackend.Inventory.Domain.model.commands.UpdateStockCommand;
import com.las4as.POSBackend.Inventory.Domain.model.entities.Category;
import com.las4as.POSBackend.Inventory.Domain.model.entities.Location;
import com.las4as.POSBackend.Inventory.Domain.model.entities.StockItem;
import com.las4as.POSBackend.Inventory.Domain.model.entities.Supplier;
import com.las4as.POSBackend.Inventory.Domain.model.events.LowStockAlertEvent;
import com.las4as.POSBackend.Inventory.Domain.model.events.ProductStockUpdatedEvent;
import com.las4as.POSBackend.Inventory.Domain.model.valueobjects.Price;
import com.las4as.POSBackend.Inventory.Domain.model.valueobjects.Quantity;
import com.las4as.POSBackend.Inventory.Domain.model.valueobjects.SKU;
import com.las4as.POSBackend.Inventory.Infrastructure.persistence.jpa.repositories.ProductRepository;
import com.las4as.POSBackend.Inventory.Infrastructure.persistence.jpa.repositories.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryDomainServiceImpl implements InventoryDomainService {
    
    private final ProductRepository productRepository;
    private final StockItemRepository stockItemRepository;
    
    @Override
    public Product createProduct(CreateProductCommand command, Category category, Supplier supplier) {
        SKU sku = new SKU(command.getSku());
        Price purchasePrice = new Price(command.getPurchasePrice());
        Price salePrice = new Price(command.getSalePrice());
        
        return new Product(
            sku, command.getName(), command.getDescription(),
            purchasePrice, salePrice, category, supplier,
            command.getUnitOfMeasure(), command.getBarcode(), command.getQrCode(),
            command.getMinimumStock(), command.getMaximumStock(),
            command.getReorderPoint(), command.getLeadTimeDays()
        );
    }
    
    @Override
    public void updateStock(Product product, Location location, UpdateStockCommand command) {
        Quantity quantity = new Quantity(command.getQuantity());
        LocalDate expirationDate = command.getExpirationDate() != null ? 
            LocalDate.parse(command.getExpirationDate()) : null;
        
        // Buscar stock item existente
        Optional<StockItem> existingStockItem = stockItemRepository
            .findByProductAndLocation(product.getId(), location.getId());
        
        if (existingStockItem.isPresent()) {
            StockItem stockItem = existingStockItem.get();
            int previousStock = stockItem.getQuantity().getValue();
            
            switch (command.getOperationType()) {
                case "ADD":
                    stockItem.addQuantity(quantity);
                    break;
                case "SUBTRACT":
                    stockItem.subtractQuantity(quantity);
                    break;
                case "SET":
                    stockItem = new StockItem(product, location, quantity, 
                        command.getBatchNumber(), expirationDate, 
                        command.getLotNumber(), command.getNotes());
                    break;
            }
            
            // Publicar evento de actualización de stock
            product.registerDomainEvent(new ProductStockUpdatedEvent(
                product.getId(), product.getSku().toString(), product.getName(),
                stockItem.getQuantity().getValue(), previousStock, location.getName()
            ));
            
            // Verificar si el stock está bajo y publicar alerta
            if (product.isLowStock()) {
                product.registerDomainEvent(new LowStockAlertEvent(
                    product.getId(), product.getSku().toString(), product.getName(),
                    product.getTotalStock(), product.getMinimumStock(), location.getName()
                ));
            }
        } else {
            // Crear nuevo stock item
            StockItem newStockItem = new StockItem(product, location, quantity,
                command.getBatchNumber(), expirationDate,
                command.getLotNumber(), command.getNotes());
            
            product.addStockItem(newStockItem);
        }
    }
    
    @Override
    public void transferStock(Product product, Location fromLocation, Location toLocation, Quantity quantity) {
        // Verificar stock disponible en origen
        int availableStock = getStockAtLocation(product, fromLocation);
        if (availableStock < quantity.getValue()) {
            throw new IllegalArgumentException("Stock insuficiente para transferir");
        }
        
        // Restar de origen
        UpdateStockCommand subtractCommand = new UpdateStockCommand(
            product.getId(), fromLocation.getId(), quantity.getValue(),
            null, null, null, "Transferencia a " + toLocation.getName(), "SUBTRACT"
        );
        updateStock(product, fromLocation, subtractCommand);
        
        // Sumar a destino
        UpdateStockCommand addCommand = new UpdateStockCommand(
            product.getId(), toLocation.getId(), quantity.getValue(),
            null, null, null, "Transferencia desde " + fromLocation.getName(), "ADD"
        );
        updateStock(product, toLocation, addCommand);
    }
    
    @Override
    public boolean isSkuTaken(SKU sku) {
        return productRepository.existsBySkuValue(sku.toString());
    }
    
    @Override
    public boolean isBarcodeTaken(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) {
            return false;
        }
        return productRepository.existsByBarcode(barcode);
    }
    
    @Override
    public int getTotalStock(Product product) {
        return product.getTotalStock();
    }
    
    @Override
    public int getStockAtLocation(Product product, Location location) {
        return stockItemRepository.findByProductAndLocation(product.getId(), location.getId())
                .map(stockItem -> stockItem.getQuantity().getValue())
                .orElse(0);
    }
    
    @Override
    public boolean isLowStock(Product product) {
        return product.isLowStock();
    }
    
    @Override
    public boolean needsReorder(Product product) {
        return product.needsReorder();
    }
    
    @Override
    public int calculateReorderQuantity(Product product) {
        // Lógica simple: reordenar hasta el máximo stock o 2x el punto de reorden
        int suggestedQuantity = product.getReorderPoint() * 2;
        
        if (product.getMaximumStock() != null && suggestedQuantity > product.getMaximumStock()) {
            suggestedQuantity = product.getMaximumStock();
        }
        
        return suggestedQuantity - product.getTotalStock();
    }
    
    @Override
    public List<Product> getLowStockProducts() {
        return productRepository.findByIsActiveTrue().stream()
                .filter(this::isLowStock)
                .toList();
    }
    
    @Override
    public List<Product> getProductsNeedingReorder() {
        return productRepository.findByIsActiveTrue().stream()
                .filter(this::needsReorder)
                .toList();
    }
    
    @Override
    public List<StockItem> getExpiringStockItems(int daysThreshold) {
        return stockItemRepository.findExpiringStockItems(daysThreshold);
    }
} 