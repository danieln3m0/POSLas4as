package com.las4as.POSBackend.IAM.Domain.services;

import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import com.las4as.POSBackend.IAM.Domain.model.commands.CreateUserCommand;
import com.las4as.POSBackend.IAM.Domain.model.entities.Role;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Email;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Password;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Username;
import com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDomainServiceImpl implements UserDomainService {
    
    private final UserRepository userRepository;
    
    @Override
    public User createUser(CreateUserCommand command) {
        return new User(
            command.getUsername(),
            command.getEmail(),
            command.getPassword(),
            command.getFirstName(),
            command.getLastName()
        );
    }
    
    @Override
    public User createUserWithRoles(CreateUserCommand command, List<Role> roles) {
        User user = new User(
            command.getUsername(),
            command.getEmail(),
            command.getPassword(),
            command.getFirstName(),
            command.getLastName()
        );
        
        // Asignar roles al usuario
        roles.forEach(user::addRole);
        
        return user;
    }
    
    @Override
    public boolean isUsernameTaken(Username username) {
        boolean exists = userRepository.existsByUsernameValue(username.toString());
        System.out.println("Checking if username '" + username.toString() + "' exists: " + exists);
        return exists;
    }
    
    @Override
    public boolean isEmailTaken(Email email) {
        boolean exists = userRepository.existsByEmailValue(email.toString());
        System.out.println("Checking if email '" + email.toString() + "' exists: " + exists);
        return exists;
    }
    
    @Override
    public boolean validateCredentials(Username username, Password password) {
        return userRepository.findByUsernameValue(username.toString())
                .map(user -> user.getPassword().toString().equals(password.toString()))
                .orElse(false);
    }
    
    @Override
    public void changePassword(User user, Password newPassword) {
        user.changePassword(newPassword);
    }
    
    @Override
    public void activateUser(User user) {
        user.activate();
    }
    
    @Override
    public void deactivateUser(User user) {
        user.deactivate();
    }
    
    @Override
    public void verifyEmail(User user) {
        user.verifyEmail();
    }
} 