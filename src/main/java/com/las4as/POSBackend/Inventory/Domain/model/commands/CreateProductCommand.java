package com.las4as.POSBackend.Inventory.Domain.model.commands;

import lombok.Getter;

@Getter
public class CreateProductCommand {
    private final String sku;
    private final String name;
    private final String description;
    private final String purchasePrice;
    private final String salePrice;
    private final Long categoryId;
    private final Long supplierId;
    private final String unitOfMeasure;
    private final String barcode;
    private final String qrCode;
    private final int minimumStock;
    private final Integer maximumStock;
    private final int reorderPoint;
    private final Integer leadTimeDays;
    
    public CreateProductCommand(String sku, String name, String description, 
                              String purchasePrice, String salePrice, Long categoryId, 
                              Long supplierId, String unitOfMeasure, String barcode, 
                              String qrCode, int minimumStock, Integer maximumStock, 
                              int reorderPoint, Integer leadTimeDays) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.purchasePrice = purchasePrice;
        this.salePrice = salePrice;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.unitOfMeasure = unitOfMeasure;
        this.barcode = barcode;
        this.qrCode = qrCode;
        this.minimumStock = minimumStock;
        this.maximumStock = maximumStock;
        this.reorderPoint = reorderPoint;
        this.leadTimeDays = leadTimeDays;
    }
} 