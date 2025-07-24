package com.las4as.POSBackend.Inventory.Infrastructure.persistence.jpa.repositories;

import com.las4as.POSBackend.Inventory.Domain.model.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    List<Category> findByIsActiveTrue();
    
    List<Category> findByParentCategoryIsNull();
    
    List<Category> findByParentCategoryId(Long parentCategoryId);
    
    boolean existsByName(String name);
} 