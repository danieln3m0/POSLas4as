package com.las4as.POSBackend.IAM.Domain.model.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserPasswordChangedEvent extends ApplicationEvent {
    private final Long userId;
    private final String username;
    
    public UserPasswordChangedEvent(Long userId, String username) {
        super(username); // Usar username como source en lugar de userId que puede ser null
        this.userId = userId;
        this.username = username;
    }
} 