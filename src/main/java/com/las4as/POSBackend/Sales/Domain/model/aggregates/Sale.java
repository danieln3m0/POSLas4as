package com.las4as.POSBackend.Sales.Domain.model.aggregates;

import com.las4as.POSBackend.Customers.Domain.model.aggregates.Customer;
import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import com.las4as.POSBackend.Sales.Domain.model.entities.Payment;
import com.las4as.POSBackend.Sales.Domain.model.entities.SaleItem;
import com.las4as.POSBackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
@Getter
@NoArgsConstructor
public class Sale extends AuditableAbstractAggregateRoot<Sale> {
    
    @Column(name = "sale_number", nullable = false, unique = true, length = 50)
    private String saleNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id", nullable = false)
    private User cashier;
    
    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;
    
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SaleStatus status;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "sale_id")
    private List<SaleItem> items = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "sale_id")
    private List<Payment> payments = new ArrayList<>();
    
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(name = "total_discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDiscount;
    
    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;
    
    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    
    @Column(name = "total_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPaid;
    
    @Column(name = "change_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal changeAmount;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    public enum SaleStatus {
        PENDING,     // Pendiente (en proceso)
        COMPLETED,   // Completada
        CANCELLED,   // Cancelada
        REFUNDED     // Devuelta
    }
    
    public Sale(String saleNumber, User cashier, Customer customer, String notes) {
        this.saleNumber = saleNumber;
        this.cashier = cashier;
        this.customer = customer;
        this.saleDate = LocalDateTime.now();
        this.status = SaleStatus.PENDING;
        this.notes = notes;
        
        // Inicializar totales en cero
        this.subtotal = BigDecimal.ZERO;
        this.totalDiscount = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.totalPaid = BigDecimal.ZERO;
        this.changeAmount = BigDecimal.ZERO;
    }
    
    public void addItem(SaleItem item) {
        this.items.add(item);
        recalculateTotals();
    }
    
    public void removeItem(SaleItem item) {
        this.items.remove(item);
        recalculateTotals();
    }
    
    public void addPayment(Payment payment) {
        if (status != SaleStatus.PENDING) {
            throw new IllegalStateException("No se pueden agregar pagos a una venta que no está pendiente");
        }
        
        this.payments.add(payment);
        recalculatePayments();
        
        // Si está completamente pagada, cambiar estado
        if (isFullyPaid()) {
            this.status = SaleStatus.COMPLETED;
        }
    }
    
    public void cancel() {
        if (status == SaleStatus.COMPLETED) {
            throw new IllegalStateException("No se puede cancelar una venta completada");
        }
        this.status = SaleStatus.CANCELLED;
    }
    
    public void refund() {
        if (status != SaleStatus.COMPLETED) {
            throw new IllegalStateException("Solo se pueden devolver ventas completadas");
        }
        this.status = SaleStatus.REFUNDED;
    }
    
    private void recalculateTotals() {
        this.subtotal = items.stream()
                .map(SaleItem::getBaseAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalDiscount = items.stream()
                .map(SaleItem::getTotalDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netAmount = subtotal.subtract(totalDiscount);
        
        // Calcular IGV (18% en Perú)
        this.taxAmount = netAmount.multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        
        this.total = netAmount.add(taxAmount);
    }
    
    private void recalculatePayments() {
        this.totalPaid = payments.stream()
                .map(payment -> payment.getAmount().getValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.changeAmount = totalPaid.compareTo(total) > 0 ? 
                totalPaid.subtract(total) : BigDecimal.ZERO;
    }
    
    public boolean isFullyPaid() {
        return totalPaid.compareTo(total) >= 0;
    }
    
    public BigDecimal getPendingAmount() {
        BigDecimal pending = total.subtract(totalPaid);
        return pending.compareTo(BigDecimal.ZERO) > 0 ? pending : BigDecimal.ZERO;
    }
    
    public boolean canAddItems() {
        return status == SaleStatus.PENDING;
    }
    
    public boolean canAddPayments() {
        return status == SaleStatus.PENDING && !isFullyPaid();
    }
    
    public int getTotalItems() {
        return items.stream().mapToInt(SaleItem::getQuantity).sum();
    }
}
