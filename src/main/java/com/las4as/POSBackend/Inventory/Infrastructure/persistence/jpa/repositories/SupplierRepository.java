package com.las4as.POSBackend.Inventory.Infrastructure.persistence.jpa.repositories;

import com.las4as.POSBackend.Inventory.Domain.model.entities.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    Optional<Supplier> findByName(String name);
    
    List<Supplier> findByIsActiveTrue();
    
    List<Supplier> findByCity(String city);
    
    List<Supplier> findByState(String state);
    
    List<Supplier> findByCountry(String country);
    
    boolean existsByName(String name);
} 