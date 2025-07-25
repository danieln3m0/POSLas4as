package com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories;

import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import com.las4as.POSBackend.IAM.Domain.model.entities.Role;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Email;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Password;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Username;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    
    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAdminUser();
    }
    
    private void initializeRoles() {
        // Verificar si faltan roles requeridos
        boolean needsInitialization = roleRepository.findByName("CASHIER").isEmpty() ||
                                    roleRepository.findByName("INVENTORY_MANAGER").isEmpty() ||
                                    roleRepository.findByName("DATA_PROTECTION_OFFICER").isEmpty();
        
        if (roleRepository.count() == 0 || needsInitialization) {
            log.info("Inicializando roles del sistema...");
            
            // Rol de Administrador
            if (roleRepository.findByName("ADMIN").isEmpty()) {
                Role adminRole = new Role("ADMIN", "Administrador del sistema con acceso completo");
                adminRole.addPermission("user:create");
                adminRole.addPermission("user:read");
                adminRole.addPermission("user:update");
                adminRole.addPermission("user:delete");
                adminRole.addPermission("role:create");
                adminRole.addPermission("role:read");
                adminRole.addPermission("role:update");
                adminRole.addPermission("role:delete");
                adminRole.addPermission("inventory:manage");
                adminRole.addPermission("reports:view");
                roleRepository.save(adminRole);
            }
            
            // Rol de Cajero
            if (roleRepository.findByName("CASHIER").isEmpty()) {
                Role cashierRole = new Role("CASHIER", "Cajero con acceso a ventas y operaciones básicas");
                cashierRole.addPermission("sales:create");
                cashierRole.addPermission("sales:read");
                cashierRole.addPermission("products:read");
                cashierRole.addPermission("inventory:read");
                roleRepository.save(cashierRole);
            }
            
            // Rol de Encargado de Inventario
            if (roleRepository.findByName("INVENTORY_MANAGER").isEmpty()) {
                Role inventoryManagerRole = new Role("INVENTORY_MANAGER", "Encargado de inventario con acceso completo al módulo");
                inventoryManagerRole.addPermission("inventory:manage");
                inventoryManagerRole.addPermission("products:manage");
                inventoryManagerRole.addPermission("suppliers:manage");
                inventoryManagerRole.addPermission("purchases:manage");
                inventoryManagerRole.addPermission("reports:view");
                roleRepository.save(inventoryManagerRole);
            }
            
            // Rol de Oficial de Protección de Datos
            if (roleRepository.findByName("DATA_PROTECTION_OFFICER").isEmpty()) {
                Role dataProtectionOfficerRole = new Role("DATA_PROTECTION_OFFICER", "Oficial de protección de datos");
                dataProtectionOfficerRole.addPermission("data:view");
                dataProtectionOfficerRole.addPermission("data:export");
                dataProtectionOfficerRole.addPermission("audit:view");
                dataProtectionOfficerRole.addPermission("privacy:manage");
                roleRepository.save(dataProtectionOfficerRole);
            }
            
            // Mantener rol de Vendedor para compatibilidad (deprecado)
            if (roleRepository.findByName("SELLER").isEmpty()) {
                Role sellerRole = new Role("SELLER", "Vendedor con acceso a ventas e inventario básico (deprecado - usar CASHIER)");
                sellerRole.addPermission("inventory:read");
                sellerRole.addPermission("sales:create");
                sellerRole.addPermission("sales:read");
                sellerRole.addPermission("products:read");
                roleRepository.save(sellerRole);
            }
            
            // Mantener rol de Inventario para compatibilidad (deprecado)
            if (roleRepository.findByName("INVENTORY").isEmpty()) {
                Role inventoryRole = new Role("INVENTORY", "Encargado de inventario (deprecado - usar INVENTORY_MANAGER)");
                inventoryRole.addPermission("inventory:manage");
                inventoryRole.addPermission("products:manage");
                inventoryRole.addPermission("suppliers:manage");
                inventoryRole.addPermission("purchases:manage");
                roleRepository.save(inventoryRole);
            }
            
            log.info("Roles inicializados correctamente");
        }
    }
    
    private void initializeAdminUser() {
        if (userRepository.count() == 0) {
            log.info("Creando usuario administrador por defecto...");
            
            try {
                User adminUser = new User(
                    new Username("admin"),
                    new Email("admin@pos.com"),
                    new Password("Admin123!"),
                    "Administrador",
                    "Sistema"
                );
                
                // Asignar rol de administrador
                Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
                adminUser.addRole(adminRole);
                adminUser.verifyEmail();
                
                userRepository.save(adminUser);
                log.info("Usuario administrador creado: admin@pos.com / Admin123!");
            } catch (Exception e) {
                log.error("Error al crear usuario administrador: {}", e.getMessage(), e);
            }
        }
    }
} 