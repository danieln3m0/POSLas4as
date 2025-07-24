package com.las4as.POSBackend.IAM.Domain.model.aggregates;

import com.las4as.POSBackend.IAM.Domain.model.entities.Role;
import com.las4as.POSBackend.IAM.Domain.model.events.UserCreatedEvent;
import com.las4as.POSBackend.IAM.Domain.model.events.UserPasswordChangedEvent;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Email;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Password;
import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Username;
import com.las4as.POSBackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User extends AuditableAbstractAggregateRoot<User> {
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "username", nullable = false, unique = true, length = 50))
    })
    private Username username;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true, length = 100))
    })
    private Email email;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "password", nullable = false))
    })
    private Password password;
    
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "is_email_verified", nullable = false)
    private boolean isEmailVerified = false;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    public User(Username username, Email email, Password password, String firstName, String lastName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        
        // Publicar evento de dominio
        registerEvent(new UserCreatedEvent(this.getId(), this.username.toString(), this.email.toString()));
    }
    
    public void changePassword(Password newPassword) {
        this.password = newPassword;
        registerEvent(new UserPasswordChangedEvent(this.getId(), this.username.toString()));
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void verifyEmail() {
        this.isEmailVerified = true;
    }
    
    public void addRole(Role role) {
        this.roles.add(role);
    }
    
    public void removeRole(Role role) {
        this.roles.remove(role);
    }
    
    public boolean hasRole(String roleName) {
        return this.roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }
    
    public boolean hasPermission(String permission) {
        return this.roles.stream().anyMatch(role -> role.hasPermission(permission));
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
} 