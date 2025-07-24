package com.las4as.POSBackend.IAM.Domain.model.queries;

import lombok.Getter;

@Getter
public class UserQuery {
    private final Long userId;
    private final String username;
    private final String email;
    
    public UserQuery(Long userId) {
        this.userId = userId;
        this.username = null;
        this.email = null;
    }
    
    public UserQuery(String username) {
        this.userId = null;
        this.username = username;
        this.email = null;
    }
    
    public UserQuery(String username, String email) {
        this.userId = null;
        this.username = username;
        this.email = email;
    }
} 