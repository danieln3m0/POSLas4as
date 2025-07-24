package com.las4as.POSBackend.Inventory.Infrastructure.persistence.jpa.repositories;

import com.las4as.POSBackend.Inventory.Domain.model.entities.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {
    
    @Query("SELECT si FROM StockItem si WHERE si.product.id = :productId AND si.location.id = :locationId")
    Optional<StockItem> findByProductAndLocation(@Param("productId") Long productId, @Param("locationId") Long locationId);
    
    @Query("SELECT si FROM StockItem si WHERE si.product.id = :productId")
    List<StockItem> findByProductId(@Param("productId") Long productId);
    
    @Query("SELECT si FROM StockItem si WHERE si.location.id = :locationId")
    List<StockItem> findByLocationId(@Param("locationId") Long locationId);
    
    @Query("SELECT si FROM StockItem si WHERE si.product.id = :productId AND si.quantity.value > 0")
    List<StockItem> findAvailableStockByProductId(@Param("productId") Long productId);
    
    @Query("SELECT si FROM StockItem si WHERE si.location.id = :locationId AND si.quantity.value > 0")
    List<StockItem> findAvailableStockByLocationId(@Param("locationId") Long locationId);
    
    @Query("SELECT si FROM StockItem si WHERE si.expirationDate <= :expirationDate AND si.quantity.value > 0")
    List<StockItem> findExpiringStockItems(@Param("expirationDate") LocalDate expirationDate);
    
    @Query("SELECT si FROM StockItem si WHERE si.expirationDate <= :thresholdDate AND si.expirationDate > :today AND si.quantity.value > 0")
    List<StockItem> findExpiringStockItems(@Param("thresholdDate") LocalDate thresholdDate, @Param("today") LocalDate today);
    
    @Query("SELECT si FROM StockItem si WHERE si.expirationDate <= :thresholdDate AND si.quantity.value > 0")
    List<StockItem> findExpiringStockItems(@Param("daysThreshold") int daysThreshold);
    
    @Query("SELECT si FROM StockItem si WHERE si.expirationDate < :today AND si.quantity.value > 0")
    List<StockItem> findExpiredStockItems(@Param("today") LocalDate today);
    
    @Query("SELECT SUM(si.quantity.value) FROM StockItem si WHERE si.product.id = :productId")
    Integer getTotalStockByProductId(@Param("productId") Long productId);
    
    @Query("SELECT SUM(si.quantity.value) FROM StockItem si WHERE si.product.id = :productId AND si.location.id = :locationId")
    Integer getStockByProductAndLocation(@Param("productId") Long productId, @Param("locationId") Long locationId);
} 