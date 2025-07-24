package com.las4as.POSBackend.Inventory.Infrastructure.persistence.jpa.repositories;

import com.las4as.POSBackend.Inventory.Domain.model.aggregates.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Query("SELECT p FROM Product p WHERE p.sku.value = :sku")
    Optional<Product> findBySkuValue(@Param("sku") String sku);
    
    @Query("SELECT p FROM Product p WHERE p.barcode = :barcode")
    Optional<Product> findByBarcode(@Param("barcode") String barcode);
    
    @Query("SELECT p FROM Product p WHERE p.qrCode = :qrCode")
    Optional<Product> findByQrCode(@Param("qrCode") String qrCode);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    List<Product> findByIsActiveTrue();
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true")
    List<Product> findByCategoryIdAndIsActiveTrue(@Param("categoryId") Long categoryId);
    
    @Query("SELECT p FROM Product p WHERE p.supplier.id = :supplierId AND p.isActive = true")
    List<Product> findBySupplierIdAndIsActiveTrue(@Param("supplierId") Long supplierId);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:searchTerm% OR p.description LIKE %:searchTerm% AND p.isActive = true")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.sku.value = :sku")
    boolean existsBySkuValue(@Param("sku") String sku);
    
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.barcode = :barcode")
    boolean existsByBarcode(@Param("barcode") String barcode);
    
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.qrCode = :qrCode")
    boolean existsByQrCode(@Param("qrCode") String qrCode);
} 