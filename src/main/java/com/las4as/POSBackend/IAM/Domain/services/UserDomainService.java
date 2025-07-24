package com.las4as.POSBackend.IAM.Domain.services;

import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import com.las4as.POSBackend.IAM.Domain.model.commands.CreateUserCommand;
import com.las4as.POSBackend.IAM.Domain.model.entities.Role;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Email;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Password;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Username;

import java.util.List;

public interface UserDomainService {
    
    /**
     * Crea un nuevo usuario
     */
    User createUser(CreateUserCommand command);
    
    /**
     * Crea un nuevo usuario con roles específicos
     */
    User createUserWithRoles(CreateUserCommand command, List<Role> roles);
    
    /**
     * Valida si un username ya existe
     */
    boolean isUsernameTaken(Username username);
    
    /**
     * Valida si un email ya existe
     */
    boolean isEmailTaken(Email email);
    
    /**
     * Valida las credenciales de un usuario
     */
    boolean validateCredentials(Username username, Password password);
    
    /**
     * Cambia la contraseña de un usuario
     */
    void changePassword(User user, Password newPassword);
    
    /**
     * Activa un usuario
     */
    void activateUser(User user);
    
    /**
     * Desactiva un usuario
     */
    void deactivateUser(User user);
    
    /**
     * Verifica el email de un usuario
     */
    void verifyEmail(User user);
} 