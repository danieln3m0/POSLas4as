package com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories;

import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.username.value = :username")
    Optional<User> findByUsernameValue(@Param("username") String username);
    
    @Query("SELECT u FROM User u WHERE u.email.value = :email")
    Optional<User> findByEmailValue(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findByIsActiveTrue();
    
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username.value = :username")
    boolean existsByUsernameValue(@Param("username") String username);
    
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email.value = :email")
    boolean existsByEmailValue(@Param("email") String email);
} 