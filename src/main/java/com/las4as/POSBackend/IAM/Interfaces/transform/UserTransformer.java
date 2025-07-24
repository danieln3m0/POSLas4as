package com.las4as.POSBackend.IAM.Interfaces.transform;

import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import org.springframework.stereotype.Component;

@Component
public class UserTransformer {
    
    public UserDTO toDTO(User user) {
        return UserDTO.fromDomain(user);
    }
} 