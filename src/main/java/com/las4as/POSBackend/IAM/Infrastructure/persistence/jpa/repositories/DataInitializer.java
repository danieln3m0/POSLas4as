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
        if (roleRepository.count() == 0) {
            log.info("Inicializando roles del sistema...");
            
            // Rol de Administrador
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
            
            // Rol de Vendedor
            Role sellerRole = new Role("SELLER", "Vendedor con acceso a ventas e inventario básico");
            sellerRole.addPermission("inventory:read");
            sellerRole.addPermission("sales:create");
            sellerRole.addPermission("sales:read");
            sellerRole.addPermission("products:read");
            roleRepository.save(sellerRole);
            
            // Rol de Inventario
            Role inventoryRole = new Role("INVENTORY", "Encargado de inventario con acceso completo al módulo");
            inventoryRole.addPermission("inventory:manage");
            inventoryRole.addPermission("products:manage");
            inventoryRole.addPermission("suppliers:manage");
            inventoryRole.addPermission("purchases:manage");
            roleRepository.save(inventoryRole);
            
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