package com.las4as.POSBackend.IAM.Domain.model.commands;

import com.las4as.POSBackend.IAM.Domain.model.valueobjects.Password;
import lombok.Getter;

@Getter
public class ChangePasswordCommand {
    private final Long userId;
    private final Password newPassword;
    
    public ChangePasswordCommand(Long userId, String newPassword) {
        this.userId = userId;
        this.newPassword = new Password(newPassword);
    }
} 