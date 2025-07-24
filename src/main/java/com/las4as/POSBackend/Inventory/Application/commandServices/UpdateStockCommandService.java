package com.las4as.POSBackend.Inventory.Application.commandServices;

import com.las4as.POSBackend.Inventory.Domain.model.aggregates.Product;
import com.las4as.POSBackend.Inventory.Domain.model.commands.UpdateStockCommand;
import com.las4as.POSBackend.Inventory.Domain.model.entities.Location;
import com.las4as.POSBackend.Inventory.Domain.services.InventoryDomainService;
import com.las4as.POSBackend.Inventory.Infrastructure.persistence.jpa.repositories.LocationRepository;
import com.las4as.POSBackend.Inventory.Infrastructure.persistence.jpa.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateStockCommandService {
    
    private final InventoryDomainService inventoryDomainService;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    
    public void execute(UpdateStockCommand command) {
        // Obtener producto
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        
        // Obtener ubicación
        Location location = locationRepository.findById(command.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Ubicación no encontrada"));
        
        // Actualizar stock usando el servicio de dominio
        inventoryDomainService.updateStock(product, location, command);
        
        // Guardar cambios
        productRepository.save(product);
    }
} 