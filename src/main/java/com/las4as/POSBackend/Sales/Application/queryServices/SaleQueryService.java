package com.las4as.POSBackend.Sales.Application.queryServices;

import com.las4as.POSBackend.Sales.Domain.model.aggregates.Sale;
import com.las4as.POSBackend.Sales.Infrastructure.persistence.jpa.repositories.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleQueryService {
    
    private final SaleRepository saleRepository;
    
    public Optional<Sale> findById(Long id) {
        return saleRepository.findById(id);
    }
    
    public Optional<Sale> findBySaleNumber(String saleNumber) {
        return saleRepository.findBySaleNumber(saleNumber);
    }
    
    public List<Sale> findByStatus(Sale.SaleStatus status) {
        return saleRepository.findByStatus(status);
    }
    
    public List<Sale> findByCashierAndDateRange(Long cashierId, LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.findByCashierAndDateRange(cashierId, startDate, endDate);
    }
    
    public List<Sale> findByCustomerId(Long customerId) {
        return saleRepository.findByCustomerId(customerId);
    }
    
    public List<Sale> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.findBySaleDateBetween(startDate, endDate);
    }
    
    public BigDecimal getTotalSalesAmount(LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.getTotalSalesAmount(startDate, endDate);
    }
    
    public Long countCompletedSales(LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.countCompletedSales(startDate, endDate);
    }
    
    public List<Sale> findPendingSales() {
        return saleRepository.findByStatus(Sale.SaleStatus.PENDING);
    }
    
    public List<Sale> findTodaySales() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return findBySaleDateBetween(startOfDay, endOfDay);
    }
}
