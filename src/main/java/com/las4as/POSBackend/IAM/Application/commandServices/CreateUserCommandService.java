package com.las4as.POSBackend.IAM.Application.commandServices;

import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import com.las4as.POSBackend.IAM.Domain.model.commands.CreateUserCommand;
import com.las4as.POSBackend.IAM.Domain.model.entities.Role;
import com.las4as.POSBackend.IAM.Domain.services.UserDomainService;
import com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories.RoleRepository;
import com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateUserCommandService {
    
    private final UserDomainService userDomainService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    public User execute(CreateUserCommand command) {
        try {
            // Validar que el username y email no existan
            if (userDomainService.isUsernameTaken(command.getUsername())) {
                throw new IllegalArgumentException("El nombre de usuario '" + command.getUsername().toString() + "' ya está en uso");
            }
            
            if (userDomainService.isEmailTaken(command.getEmail())) {
                throw new IllegalArgumentException("El email '" + command.getEmail().toString() + "' ya está en uso");
            }
            
            // Validar y obtener los roles
            List<Role> roles = validateAndGetRoles(command.getRoleNames());
            
            // Crear el usuario usando el servicio de dominio con roles
            User user = userDomainService.createUserWithRoles(command, roles);
            
            // Guardar en el repositorio
            User savedUser = userRepository.save(user);
            
            // Log para debugging
            System.out.println("Usuario creado exitosamente: " + savedUser.getUsername().toString());
            
            return savedUser;
        } catch (IllegalArgumentException e) {
            // Re-lanzar errores de validación
            throw e;
        } catch (Exception e) {
            // Log del error para debugging
            System.err.println("Error al crear usuario: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al crear el usuario: " + e.getMessage(), e);
        }
    }
    
    private List<Role> validateAndGetRoles(List<String> roleNames) {
        List<Role> roles = roleRepository.findByNameIn(roleNames);
        
        if (roles.size() != roleNames.size()) {
            List<String> foundRoleNames = roles.stream().map(Role::getName).toList();
            List<String> missingRoles = roleNames.stream()
                    .filter(name -> !foundRoleNames.contains(name))
                    .toList();
            
            throw new IllegalArgumentException("Los siguientes roles no existen: " + String.join(", ", missingRoles));
        }
        
        return roles;
    }
} 