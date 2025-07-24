package com.las4as.POSBackend.Inventory.Application.commandServices;

import com.las4as.POSBackend.Inventory.Domain.model.aggregates.Product;
import com.las4as.POSBackend.Inventory.Domain.model.commands.CreateProductCommand;
import com.las4as.POSBackend.Inventory.Domain.model.entities.Category;
import com.las4as.POSBackend.Inventory.Domain.model.entities.Supplier;
import com.las4as.POSBackend.Inventory.Domain.model.valueobjects.SKU;
import com.las4as.POSBackend.Inventory.Domain.services.InventoryDomainService;
import com.las4as.POSBackend.Inventory.Infrastructure.persistence.jpa.repositories.CategoryRepository;
import com.las4as.POSBackend.Inventory.Infrastructure.persistence.jpa.repositories.ProductRepository;
import com.las4as.POSBackend.Inventory.Infrastructure.persistence.jpa.repositories.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateProductCommandService {
    
    private final InventoryDomainService inventoryDomainService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    
    public Product execute(CreateProductCommand command) {
        // Validar que el SKU no exista
        SKU sku = new SKU(command.getSku());
        if (inventoryDomainService.isSkuTaken(sku)) {
            throw new IllegalArgumentException("El SKU ya está en uso");
        }
        
        // Validar que el código de barras no exista (si se proporciona)
        if (command.getBarcode() != null && !command.getBarcode().trim().isEmpty()) {
            if (inventoryDomainService.isBarcodeTaken(command.getBarcode())) {
                throw new IllegalArgumentException("El código de barras ya está en uso");
            }
        }
        
        // Obtener categoría
        Category category = null;
        if (command.getCategoryId() != null) {
            category = categoryRepository.findById(command.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        }
        
        // Obtener proveedor
        Supplier supplier = null;
        if (command.getSupplierId() != null) {
            supplier = supplierRepository.findById(command.getSupplierId())
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        }
        
        // Crear el producto usando el servicio de dominio
        Product product = inventoryDomainService.createProduct(command, category, supplier);
        
        // Guardar en el repositorio
        return productRepository.save(product);
    }
} 