package com.las4as.POSBackend.Sales.Interfaces.transform;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaleDTO {
    private Long id;
    private String saleNumber;
    private Long customerId;
    private String customerName;
    private String customerDocument;
    private Long cashierId;
    private String cashierName;
    private LocalDateTime saleDate;
    private String status;
    private List<SaleItemDTO> items;
    private List<PaymentDTO> payments;
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private BigDecimal totalPaid;
    private BigDecimal changeAmount;
    private BigDecimal pendingAmount;
    private int totalItems;
    private String notes;
    
    @Data
    public static class SaleItemDTO {
        private Long id;
        private Long productId;
        private String productName;
        private String productSku;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal discountPercentage;
        private BigDecimal discountAmount;
        private String discountType;
        private BigDecimal subtotal;
        private String notes;
    }
    
    @Data
    public static class PaymentDTO {
        private Long id;
        private String paymentMethod;
        private String paymentMethodName;
        private BigDecimal amount;
        private String referenceNumber;
        private String notes;
    }
}
