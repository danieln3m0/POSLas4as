package com.las4as.POSBackend.Sales.Interfaces.transform;

import com.las4as.POSBackend.Sales.Domain.model.aggregates.Sale;
import com.las4as.POSBackend.Sales.Domain.model.entities.Payment;
import com.las4as.POSBackend.Sales.Domain.model.entities.SaleItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SaleTransformer {
    
    public SaleDTO toDTO(Sale sale) {
        SaleDTO dto = new SaleDTO();
        dto.setId(sale.getId());
        dto.setSaleNumber(sale.getSaleNumber());
        
        if (sale.getCustomer() != null) {
            dto.setCustomerId(sale.getCustomer().getId());
            dto.setCustomerName(sale.getCustomer().getFullName());
            dto.setCustomerDocument(sale.getCustomer().getDocumentNumber().toString());
        }
        
        dto.setCashierId(sale.getCashier().getId());
        dto.setCashierName(sale.getCashier().getFullName());
        dto.setSaleDate(sale.getSaleDate());
        dto.setStatus(sale.getStatus().name());
        
        // Convertir items
        List<SaleDTO.SaleItemDTO> itemDTOs = sale.getItems().stream()
                .map(this::toSaleItemDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);
        
        // Convertir payments
        List<SaleDTO.PaymentDTO> paymentDTOs = sale.getPayments().stream()
                .map(this::toPaymentDTO)
                .collect(Collectors.toList());
        dto.setPayments(paymentDTOs);
        
        dto.setSubtotal(sale.getSubtotal());
        dto.setTotalDiscount(sale.getTotalDiscount());
        dto.setTaxAmount(sale.getTaxAmount());
        dto.setTotal(sale.getTotal());
        dto.setTotalPaid(sale.getTotalPaid());
        dto.setChangeAmount(sale.getChangeAmount());
        dto.setPendingAmount(sale.getPendingAmount());
        dto.setTotalItems(sale.getTotalItems());
        dto.setNotes(sale.getNotes());
        
        return dto;
    }
    
    private SaleDTO.SaleItemDTO toSaleItemDTO(SaleItem item) {
        SaleDTO.SaleItemDTO dto = new SaleDTO.SaleItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductSku(item.getProduct().getSku().toString());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        
        if (item.getDiscount() != null) {
            dto.setDiscountPercentage(item.getDiscount().getPercentage());
            dto.setDiscountAmount(item.getDiscount().getAmount());
            dto.setDiscountType(item.getDiscount().getType());
        }
        
        dto.setSubtotal(item.getSubtotal());
        dto.setNotes(item.getNotes());
        
        return dto;
    }
    
    private SaleDTO.PaymentDTO toPaymentDTO(Payment payment) {
        SaleDTO.PaymentDTO dto = new SaleDTO.PaymentDTO();
        dto.setId(payment.getId());
        dto.setPaymentMethod(payment.getPaymentMethod().name());
        dto.setPaymentMethodName(payment.getPaymentMethod().getDisplayName());
        dto.setAmount(payment.getAmount().getValue());
        dto.setReferenceNumber(payment.getReferenceNumber());
        dto.setNotes(payment.getNotes());
        
        return dto;
    }
}
