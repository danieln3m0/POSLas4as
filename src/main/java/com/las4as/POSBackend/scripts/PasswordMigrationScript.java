package com.las4as.POSBackend.scripts;

import com.las4as.POSBackend.IAM.Application.outboundServices.HashingService;
import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Password;
import com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Script para migrar contraseñas en texto plano a BCrypt
 * Se ejecutará automáticamente al iniciar la aplicación
 */
@Component
@RequiredArgsConstructor
public class PasswordMigrationScript implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final HashingService hashingService;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Iniciando migración de contraseñas ===");
        
        List<User> allUsers = userRepository.findAll();
        int migratedCount = 0;
        
        for (User user : allUsers) {
            String currentPassword = user.getPassword().toString();
            
            // Verificar si la contraseña ya está hasheada con BCrypt
            // Las contraseñas BCrypt empiezan con $2a$, $2b$, $2x$, o $2y$
            if (!currentPassword.startsWith("$2")) {
                System.out.println("Migrando contraseña para usuario: " + user.getUsername().toString());
                
                // Hashear la contraseña en texto plano
                String hashedPassword = hashingService.hash(currentPassword);
                Password newPassword = Password.fromHash(hashedPassword);
                
                // Actualizar el usuario con la nueva contraseña hasheada
                user.changePassword(newPassword);
                userRepository.save(user);
                
                migratedCount++;
                System.out.println("✓ Contraseña migrada para: " + user.getUsername().toString());
            } else {
                System.out.println("✓ Usuario " + user.getUsername().toString() + " ya tiene contraseña hasheada");
            }
        }
        
        System.out.println("=== Migración completada ===");
        System.out.println("Usuarios migrados: " + migratedCount + " de " + allUsers.size());
    }
}
