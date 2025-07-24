package com.las4as.POSBackend.IAM.Domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserCreatedEvent extends ApplicationEvent {
    private final Long userId;
    private final String username;
    private final String email;
    
    public UserCreatedEvent(Long userId, String username, String email) {
        super(username); // Usar username como source en lugar de userId que puede ser null
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
} 