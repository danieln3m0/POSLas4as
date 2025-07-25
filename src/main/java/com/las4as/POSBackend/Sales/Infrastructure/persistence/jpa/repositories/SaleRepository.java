package com.las4as.POSBackend.Sales.Infrastructure.persistence.jpa.repositories;

import com.las4as.POSBackend.Sales.Domain.model.aggregates.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    
    Optional<Sale> findBySaleNumber(String saleNumber);
    
    boolean existsBySaleNumber(String saleNumber);
    
    @Query("SELECT s FROM Sale s WHERE s.status = :status ORDER BY s.saleDate DESC")
    List<Sale> findByStatus(@Param("status") Sale.SaleStatus status);
    
    @Query("SELECT s FROM Sale s WHERE s.cashier.id = :cashierId AND s.saleDate BETWEEN :startDate AND :endDate ORDER BY s.saleDate DESC")
    List<Sale> findByCashierAndDateRange(@Param("cashierId") Long cashierId, 
                                        @Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s FROM Sale s WHERE s.customer.id = :customerId ORDER BY s.saleDate DESC")
    List<Sale> findByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :startDate AND :endDate ORDER BY s.saleDate DESC")
    List<Sale> findBySaleDateBetween(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COALESCE(SUM(s.total), 0) FROM Sale s WHERE s.status = 'COMPLETED' AND s.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSalesAmount(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.status = 'COMPLETED' AND s.saleDate BETWEEN :startDate AND :endDate")
    Long countCompletedSales(@Param("startDate") LocalDateTime startDate, 
                            @Param("endDate") LocalDateTime endDate);
}
