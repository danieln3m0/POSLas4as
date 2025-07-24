package com.las4as.POSBackend.IAM.Application.queryServices;

import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {
    
    private final UserRepository userRepository;
    
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameValue(username);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailValue(email);
    }
    
    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    public List<User> findActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }
    
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsernameValue(username);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailValue(email);
    }
} 