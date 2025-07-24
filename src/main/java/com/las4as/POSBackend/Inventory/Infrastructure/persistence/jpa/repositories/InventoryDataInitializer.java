package com.las4as.POSBackend.Inventory.Infrastructure.persistence.jpa.repositories;

import com.las4as.POSBackend.Inventory.Domain.model.entities.Category;
import com.las4as.POSBackend.Inventory.Domain.model.entities.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Ejecutar después del DataInitializer de IAM
public class InventoryDataInitializer implements CommandLineRunner {
    
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    
    @Override
    public void run(String... args) throws Exception {
        initializeCategories();
        initializeSuppliers();
    }
    
    private void initializeCategories() {
        if (categoryRepository.count() == 0) {
            log.info("Inicializando categorías del sistema...");
            
            // Categorías principales
            Category electronicsCategory = new Category("Electrónicos", "Productos electrónicos y tecnológicos");
            categoryRepository.save(electronicsCategory);
            
            Category homeCategory = new Category("Hogar", "Productos para el hogar y limpieza");
            categoryRepository.save(homeCategory);
            
            Category officeCategory = new Category("Oficina", "Productos de oficina y papelería");
            categoryRepository.save(officeCategory);
            
            Category foodCategory = new Category("Alimentación", "Productos alimenticios y bebidas");
            categoryRepository.save(foodCategory);
            
            Category clothingCategory = new Category("Ropa", "Prendas de vestir y accesorios");
            categoryRepository.save(clothingCategory);
            
            log.info("Categorías inicializadas correctamente");
        }
    }
    
    private void initializeSuppliers() {
        if (supplierRepository.count() == 0) {
            log.info("Inicializando proveedores del sistema...");
            
            Supplier supplier1 = new Supplier(
                "TechSupply S.A.",
                "Juan Pérez",
                "tech@techsupply.com",
                "+51-123-456789",
                "Av. Tecnología 123",
                "Lima",
                "Lima",
                "15001",
                "Perú",
                "20123456789",
                "30 días",
                "Proveedor de productos tecnológicos"
            );
            supplierRepository.save(supplier1);
            
            Supplier supplier2 = new Supplier(
                "HomeProducts Corp",
                "María García",
                "ventas@homeproducts.com",
                "+51-987-654321",
                "Jr. Hogar 456",
                "Lima",
                "Lima", 
                "15002",
                "Perú",
                "20987654321",
                "15 días",
                "Proveedor de productos para el hogar"
            );
            supplierRepository.save(supplier2);
            
            Supplier supplier3 = new Supplier(
                "Oficina Total",
                "Carlos Rodríguez",
                "contacto@oficinatotal.com", 
                "+51-555-123456",
                "Av. Oficina 789",
                "Lima",
                "Lima",
                "15003",
                "Perú",
                "20555123456",
                "45 días",
                "Proveedor de productos de oficina"
            );
            supplierRepository.save(supplier3);
            
            log.info("Proveedores inicializados correctamente");
        }
    }
}
