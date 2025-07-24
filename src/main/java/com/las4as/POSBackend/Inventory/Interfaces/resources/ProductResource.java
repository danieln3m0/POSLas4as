package com.las4as.POSBackend.Inventory.Interfaces.resources;

import com.las4as.POSBackend.Inventory.Application.commandServices.CreateProductCommandService;
import com.las4as.POSBackend.Inventory.Application.commandServices.UpdateStockCommandService;
import com.las4as.POSBackend.Inventory.Application.queryServices.ProductQueryService;
import com.las4as.POSBackend.Inventory.Domain.model.aggregates.Product;
import com.las4as.POSBackend.Inventory.Domain.model.commands.CreateProductCommand;
import com.las4as.POSBackend.Inventory.Domain.model.commands.UpdateStockCommand;
import com.las4as.POSBackend.Inventory.Interfaces.transform.ProductDTO;
import com.las4as.POSBackend.Inventory.Interfaces.transform.ProductTransformer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/inventory/products")
@RequiredArgsConstructor
@Tag(name = "Gestión de Productos", description = "Endpoints para la gestión de productos del inventario")
public class ProductResource {
    
    private final CreateProductCommandService createProductCommandService;
    private final UpdateStockCommandService updateStockCommandService;
    private final ProductQueryService productQueryService;
    private final ProductTransformer productTransformer;
    
    @PostMapping
    @Operation(summary = "Crear producto", description = "Crea un nuevo producto en el inventario")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody CreateProductRequest request) {
        try {
            CreateProductCommand command = new CreateProductCommand(
                request.getSku(),
                request.getName(),
                request.getDescription(),
                request.getPurchasePrice(),
                request.getSalePrice(),
                request.getCategoryId(),
                request.getSupplierId(),
                request.getUnitOfMeasure(),
                request.getBarcode(),
                request.getQrCode(),
                request.getMinimumStock(),
                request.getMaximumStock(),
                request.getReorderPoint(),
                request.getLeadTimeDays()
            );
            
            Product product = createProductCommandService.execute(command);
            ProductDTO productDTO = productTransformer.toDTO(product);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(productDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{productId}")
    @Operation(summary = "Obtener producto por ID", description = "Obtiene la información de un producto específico")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
        return productQueryService.findById(productId)
                .map(product -> ResponseEntity.ok(productTransformer.toDTO(product)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/sku/{sku}")
    @Operation(summary = "Obtener producto por SKU", description = "Obtiene la información de un producto por su SKU")
    public ResponseEntity<ProductDTO> getProductBySku(@PathVariable String sku) {
        return productQueryService.findBySku(sku)
                .map(product -> ResponseEntity.ok(productTransformer.toDTO(product)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(summary = "Listar productos", description = "Obtiene la lista de todos los productos activos")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productQueryService.findAll();
        List<ProductDTO> productDTOs = products.stream()
                .map(productTransformer::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDTOs);
    }
    
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Productos por categoría", description = "Obtiene productos de una categoría específica")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Product> products = productQueryService.findByCategory(categoryId);
        List<ProductDTO> productDTOs = products.stream()
                .map(productTransformer::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDTOs);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Buscar productos", description = "Busca productos por nombre o descripción")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String q) {
        List<Product> products = productQueryService.searchProducts(q);
        List<ProductDTO> productDTOs = products.stream()
                .map(productTransformer::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDTOs);
    }
    
    @GetMapping("/low-stock")
    @Operation(summary = "Productos con stock bajo", description = "Obtiene productos que tienen stock bajo")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts() {
        List<Product> products = productQueryService.getLowStockProducts();
        List<ProductDTO> productDTOs = products.stream()
                .map(productTransformer::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDTOs);
    }
    
    @GetMapping("/needing-reorder")
    @Operation(summary = "Productos que necesitan reorden", description = "Obtiene productos que necesitan reorden")
    public ResponseEntity<List<ProductDTO>> getProductsNeedingReorder() {
        List<Product> products = productQueryService.getProductsNeedingReorder();
        List<ProductDTO> productDTOs = products.stream()
                .map(productTransformer::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDTOs);
    }
    
    @PostMapping("/{productId}/stock")
    @Operation(summary = "Actualizar stock", description = "Actualiza el stock de un producto en una ubicación")
    public ResponseEntity<Void> updateStock(@PathVariable Long productId, @RequestBody UpdateStockRequest request) {
        try {
            UpdateStockCommand command = new UpdateStockCommand(
                productId,
                request.getLocationId(),
                request.getQuantity(),
                request.getBatchNumber(),
                request.getExpirationDate(),
                request.getLotNumber(),
                request.getNotes(),
                request.getOperationType()
            );
            
            updateStockCommandService.execute(command);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{productId}/stock")
    @Operation(summary = "Obtener stock del producto", description = "Obtiene el stock total de un producto")
    public ResponseEntity<StockInfo> getProductStock(@PathVariable Long productId) {
        return productQueryService.findById(productId)
                .map(product -> {
                    int totalStock = productQueryService.getTotalStock(product);
                    boolean isLowStock = productQueryService.isLowStock(product);
                    boolean needsReorder = productQueryService.needsReorder(product);
                    int reorderQuantity = productQueryService.calculateReorderQuantity(product);
                    
                    StockInfo stockInfo = new StockInfo();
                    stockInfo.setTotalStock(totalStock);
                    stockInfo.setLowStock(isLowStock);
                    stockInfo.setNeedsReorder(needsReorder);
                    stockInfo.setReorderQuantity(reorderQuantity);
                    
                    return ResponseEntity.ok(stockInfo);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Clases internas para requests y responses
    public static class CreateProductRequest {
        private String sku;
        private String name;
        private String description;
        private String purchasePrice;
        private String salePrice;
        private Long categoryId;
        private Long supplierId;
        private String unitOfMeasure;
        private String barcode;
        private String qrCode;
        private int minimumStock;
        private Integer maximumStock;
        private int reorderPoint;
        private Integer leadTimeDays;
        
        // Getters y setters
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getPurchasePrice() { return purchasePrice; }
        public void setPurchasePrice(String purchasePrice) { this.purchasePrice = purchasePrice; }
        
        public String getSalePrice() { return salePrice; }
        public void setSalePrice(String salePrice) { this.salePrice = salePrice; }
        
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        
        public Long getSupplierId() { return supplierId; }
        public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
        
        public String getUnitOfMeasure() { return unitOfMeasure; }
        public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }
        
        public String getBarcode() { return barcode; }
        public void setBarcode(String barcode) { this.barcode = barcode; }
        
        public String getQrCode() { return qrCode; }
        public void setQrCode(String qrCode) { this.qrCode = qrCode; }
        
        public int getMinimumStock() { return minimumStock; }
        public void setMinimumStock(int minimumStock) { this.minimumStock = minimumStock; }
        
        public Integer getMaximumStock() { return maximumStock; }
        public void setMaximumStock(Integer maximumStock) { this.maximumStock = maximumStock; }
        
        public int getReorderPoint() { return reorderPoint; }
        public void setReorderPoint(int reorderPoint) { this.reorderPoint = reorderPoint; }
        
        public Integer getLeadTimeDays() { return leadTimeDays; }
        public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    }
    
    public static class UpdateStockRequest {
        private Long locationId;
        private int quantity;
        private String batchNumber;
        private String expirationDate;
        private String lotNumber;
        private String notes;
        private String operationType;
        
        // Getters y setters
        public Long getLocationId() { return locationId; }
        public void setLocationId(Long locationId) { this.locationId = locationId; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        
        public String getBatchNumber() { return batchNumber; }
        public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
        
        public String getExpirationDate() { return expirationDate; }
        public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
        
        public String getLotNumber() { return lotNumber; }
        public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        
        public String getOperationType() { return operationType; }
        public void setOperationType(String operationType) { this.operationType = operationType; }
    }
    
    public static class StockInfo {
        private int totalStock;
        private boolean lowStock;
        private boolean needsReorder;
        private int reorderQuantity;
        
        // Getters y setters
        public int getTotalStock() { return totalStock; }
        public void setTotalStock(int totalStock) { this.totalStock = totalStock; }
        
        public boolean isLowStock() { return lowStock; }
        public void setLowStock(boolean lowStock) { this.lowStock = lowStock; }
        
        public boolean isNeedsReorder() { return needsReorder; }
        public void setNeedsReorder(boolean needsReorder) { this.needsReorder = needsReorder; }
        
        public int getReorderQuantity() { return reorderQuantity; }
        public void setReorderQuantity(int reorderQuantity) { this.reorderQuantity = reorderQuantity; }
    }
} 