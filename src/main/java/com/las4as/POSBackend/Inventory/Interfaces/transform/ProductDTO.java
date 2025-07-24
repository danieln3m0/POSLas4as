package com.las4as.POSBackend.Inventory.Interfaces.transform;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private Long categoryId;
    private String categoryName;
    private Long supplierId;
    private String supplierName;
    private String unitOfMeasure;
    private String barcode;
    private String qrCode;
    private int minimumStock;
    private Integer maximumStock;
    private int reorderPoint;
    private Integer leadTimeDays;
    private boolean isActive;
    private int totalStock;
    private boolean lowStock;
    private boolean needsReorder;
    private double profitMargin;
    private Date createdAt;
    private Date updatedAt;
    
    public static ProductDTO fromDomain(com.las4as.POSBackend.Inventory.Domain.model.aggregates.Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku().toString());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPurchasePrice(product.getPurchasePrice().getValue());
        dto.setSalePrice(product.getSalePrice().getValue());
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        if (product.getSupplier() != null) {
            dto.setSupplierId(product.getSupplier().getId());
            dto.setSupplierName(product.getSupplier().getName());
        }
        
        dto.setUnitOfMeasure(product.getUnitOfMeasure());
        dto.setBarcode(product.getBarcode());
        dto.setQrCode(product.getQrCode());
        dto.setMinimumStock(product.getMinimumStock());
        dto.setMaximumStock(product.getMaximumStock());
        dto.setReorderPoint(product.getReorderPoint());
        dto.setLeadTimeDays(product.getLeadTimeDays());
        dto.setActive(product.isActive());
        dto.setTotalStock(product.getTotalStock());
        dto.setLowStock(product.isLowStock());
        dto.setNeedsReorder(product.needsReorder());
        dto.setProfitMargin(product.getProfitMargin());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        return dto;
    }
} 