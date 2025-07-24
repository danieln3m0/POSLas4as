package com.las4as.POSBackend.IAM.Domain.model.commands;

import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Email;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Password;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Username;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateUserCommand {
    private final Username username;
    private final Email email;
    private final Password password;
    private final String firstName;
    private final String lastName;
    private final List<String> roleNames;
    
    public CreateUserCommand(String username, String email, String password, String firstName, String lastName, List<String> roleNames) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío");
        }
        if (roleNames == null || roleNames.isEmpty()) {
            throw new IllegalArgumentException("Debe especificar al menos un rol");
        }
        
        this.username = new Username(username.trim());
        this.email = new Email(email.trim());
        this.password = new Password(password);
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.roleNames = roleNames;
    }
    
    // Constructor sin roles para mantener compatibilidad (asigna rol USER por defecto)
    public CreateUserCommand(String username, String email, String password, String firstName, String lastName) {
        this(username, email, password, firstName, lastName, List.of("USER"));
    }
} 