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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(
        summary = "Crear un nuevo producto", 
        description = "Crea un nuevo producto en el sistema de inventario con validaciones de negocio completas",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del producto a crear",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreateProductRequest.class),
                examples = @ExampleObject(
                    name = "Ejemplo de Producto",
                    value = """
                    {
                      "sku": "LAPTOP-DELL-002",
                      "name": "Laptop Dell Inspiron 16",
                      "description": "Laptop Dell Inspiron 16, Intel i5, 16GB RAM, 512GB SSD",
                      "purchasePrice": "600.00",
                      "salePrice": "850.00",
                      "categoryId": 2,
                      "supplierId": 1,
                      "unitOfMeasure": "UNIT",
                      "barcode": "1234567890124",
                      "minimumStock": 3,
                      "maximumStock": 30,
                      "reorderPoint": 8,
                      "leadTimeDays": 5
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Producto creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos en la solicitud (SKU duplicado, categoría/proveedor inexistente, etc.)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token de autorización requerido"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "No tiene permisos para crear productos"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    public ResponseEntity<ProductDTO> createProduct(@RequestBody CreateProductRequest request) {
        try {
            System.out.println("DEBUG: Iniciando creación de producto con SKU: " + request.getSku());
            
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
            
            System.out.println("DEBUG: Comando creado, ejecutando servicio...");
            Product product = createProductCommandService.execute(command);
            System.out.println("DEBUG: Producto creado con ID: " + product.getId());
            
            ProductDTO productDTO = productTransformer.toDTO(product);
            System.out.println("DEBUG: DTO transformado exitosamente");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(productDTO);
        } catch (IllegalArgumentException e) {
            System.err.println("DEBUG: IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("DEBUG: Exception inesperada: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{productId}")
    @Operation(
        summary = "Obtener producto por ID", 
        description = "Recupera la información completa de un producto específico mediante su identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Producto encontrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado con el ID especificado"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token de autorización requerido"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "No tiene permisos para consultar productos"
        )
    })
    public ResponseEntity<ProductDTO> getProductById(
        @Parameter(description = "ID único del producto", example = "1", required = true)
        @PathVariable Long productId) {
        return productQueryService.findById(productId)
                .map(product -> ResponseEntity.ok(productTransformer.toDTO(product)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/sku/{sku}")
    @Operation(
        summary = "Obtener producto por SKU", 
        description = "Recupera la información de un producto específico mediante su código SKU (Stock Keeping Unit)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Producto encontrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado con el SKU especificado"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token de autorización requerido"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "No tiene permisos para consultar productos"
        )
    })
    public ResponseEntity<ProductDTO> getProductBySku(
        @Parameter(description = "Código SKU del producto", example = "LAPTOP-DELL-001", required = true)
        @PathVariable String sku) {
        return productQueryService.findBySku(sku)
                .map(product -> ResponseEntity.ok(productTransformer.toDTO(product)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    @Operation(
        summary = "Listar todos los productos", 
        description = "Obtiene la lista completa de todos los productos activos en el sistema de inventario"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de productos recuperada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token de autorización requerido"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "No tiene permisos para consultar productos"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productQueryService.findAll();
        List<ProductDTO> productDTOs = products.stream()
                .map(productTransformer::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDTOs);
    }
    
    @GetMapping("/category/{categoryId}")
    @Operation(
        summary = "Obtener productos por categoría", 
        description = "Recupera todos los productos que pertenecen a una categoría específica"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de productos de la categoría recuperada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Categoría no encontrada o sin productos"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token de autorización requerido"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "No tiene permisos para consultar productos"
        )
    })
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(
        @Parameter(description = "ID único de la categoría", example = "2", required = true)
        @PathVariable Long categoryId) {
        List<Product> products = productQueryService.findByCategory(categoryId);
        List<ProductDTO> productDTOs = products.stream()
                .map(productTransformer::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDTOs);
    }
    
    @GetMapping("/search")
    @Operation(
        summary = "Buscar productos por texto", 
        description = "Realiza una búsqueda de productos por nombre, descripción o SKU utilizando coincidencias parciales"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resultados de búsqueda obtenidos exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetro de búsqueda inválido o vacío"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token de autorización requerido"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "No tiene permisos para buscar productos"
        )
    })
    public ResponseEntity<List<ProductDTO>> searchProducts(
        @Parameter(description = "Término de búsqueda (nombre, descripción o SKU)", example = "laptop", required = true)
        @RequestParam String q) {
        List<Product> products = productQueryService.searchProducts(q);
        List<ProductDTO> productDTOs = products.stream()
                .map(productTransformer::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDTOs);
    }
    
    @GetMapping("/low-stock")
    @Operation(
        summary = "Productos con stock bajo", 
        description = "Obtiene todos los productos que tienen stock por debajo del mínimo configurado, útil para gestión de inventario"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de productos con stock bajo obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token de autorización requerido"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "No tiene permisos para consultar reportes de inventario"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    public ResponseEntity<List<ProductDTO>> getLowStockProducts() {
        List<Product> products = productQueryService.getLowStockProducts();
        List<ProductDTO> productDTOs = products.stream()
                .map(productTransformer::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDTOs);
    }
    
    @GetMapping("/needing-reorder")
    @Operation(
        summary = "Productos que requieren reorden", 
        description = "Obtiene productos que han alcanzado el punto de reorden y necesitan ser reabastecidos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de productos que necesitan reorden obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token de autorización requerido"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "No tiene permisos para consultar reportes de reorden"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    public ResponseEntity<List<ProductDTO>> getProductsNeedingReorder() {
        List<Product> products = productQueryService.getProductsNeedingReorder();
        List<ProductDTO> productDTOs = products.stream()
                .map(productTransformer::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDTOs);
    }
    
    @PostMapping("/{productId}/stock")
    @Operation(
        summary = "Actualizar stock de producto", 
        description = "Registra una operación de entrada (IN) o salida (OUT) de stock para un producto en una ubicación específica",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos de la operación de stock",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UpdateStockRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Entrada de Stock",
                        value = """
                        {
                          "locationId": 1,
                          "quantity": 50,
                          "batchNumber": "BATCH001",
                          "expirationDate": "2025-12-31",
                          "lotNumber": "LOT001",
                          "notes": "Recepción de mercancía del proveedor",
                          "operationType": "IN"
                        }
                        """
                    ),
                    @ExampleObject(
                        name = "Salida de Stock",
                        value = """
                        {
                          "locationId": 1,
                          "quantity": 5,
                          "batchNumber": "BATCH001",
                          "notes": "Venta - Factura #001",
                          "operationType": "OUT"
                        }
                        """
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Stock actualizado exitosamente"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos inválidos (producto inexistente, ubicación inválida, cantidad negativa, etc.)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token de autorización requerido"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "No tiene permisos para actualizar stock"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    public ResponseEntity<Void> updateStock(
        @Parameter(description = "ID único del producto", example = "1", required = true)
        @PathVariable Long productId, 
        @RequestBody UpdateStockRequest request) {
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
    @Operation(
        summary = "Consultar stock de producto", 
        description = "Obtiene información detallada del stock actual de un producto, incluyendo estado de inventario y alertas"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Información de stock obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = StockInfo.class),
                examples = @ExampleObject(
                    name = "Ejemplo de respuesta",
                    value = """
                    {
                      "totalStock": 50,
                      "lowStock": false,
                      "needsReorder": false,
                      "reorderQuantity": -30
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token de autorización requerido"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "No tiene permisos para consultar stock"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    public ResponseEntity<StockInfo> getProductStock(
        @Parameter(description = "ID único del producto", example = "1", required = true)
        @PathVariable Long productId) {
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
    @Schema(description = "Datos requeridos para crear un nuevo producto")
    public static class CreateProductRequest {
        @Schema(description = "Código SKU único del producto", example = "LAPTOP-DELL-001", required = true)
        private String sku;
        
        @Schema(description = "Nombre del producto", example = "Laptop Dell Inspiron", required = true)
        private String name;
        
        @Schema(description = "Descripción detallada del producto", example = "Laptop Dell Inspiron 15 3000, Intel i3, 8GB RAM, 256GB SSD")
        private String description;
        
        @Schema(description = "Precio de compra", example = "500.00", required = true)
        private String purchasePrice;
        
        @Schema(description = "Precio de venta", example = "750.00", required = true)
        private String salePrice;
        
        @Schema(description = "ID de la categoría del producto", example = "2", required = true)
        private Long categoryId;
        
        @Schema(description = "ID del proveedor del producto", example = "1", required = true)
        private Long supplierId;
        
        @Schema(description = "Unidad de medida", example = "UNIT", allowableValues = {"UNIT", "KG", "LITER", "METER"}, required = true)
        private String unitOfMeasure;
        
        @Schema(description = "Código de barras del producto", example = "1234567890123")
        private String barcode;
        
        @Schema(description = "Código QR del producto")
        private String qrCode;
        
        @Schema(description = "Stock mínimo requerido", example = "5", required = true)
        private int minimumStock;
        
        @Schema(description = "Stock máximo permitido", example = "50")
        private Integer maximumStock;
        
        @Schema(description = "Punto de reorden", example = "10", required = true)
        private int reorderPoint;
        
        @Schema(description = "Días de tiempo de entrega", example = "7")
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
    
    @Schema(description = "Datos requeridos para actualizar el stock de un producto")
    public static class UpdateStockRequest {
        @Schema(description = "ID de la ubicación donde se realiza la operación", example = "1", required = true)
        private Long locationId;
        
        @Schema(description = "Cantidad a agregar (IN) o quitar (OUT)", example = "50", required = true)
        private int quantity;
        
        @Schema(description = "Número de lote del producto", example = "BATCH001")
        private String batchNumber;
        
        @Schema(description = "Fecha de expiración (formato: YYYY-MM-DD)", example = "2025-12-31")
        private String expirationDate;
        
        @Schema(description = "Número de lote interno", example = "LOT001")
        private String lotNumber;
        
        @Schema(description = "Notas adicionales sobre la operación", example = "Recepción de mercancía del proveedor")
        private String notes;
        
        @Schema(description = "Tipo de operación", example = "IN", allowableValues = {"IN", "OUT"}, required = true)
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
    
    @Schema(description = "Información detallada del stock de un producto")
    public static class StockInfo {
        @Schema(description = "Cantidad total en stock", example = "50")
        private int totalStock;
        
        @Schema(description = "Indica si el stock está por debajo del mínimo", example = "false")
        private boolean lowStock;
        
        @Schema(description = "Indica si el producto necesita ser reordenado", example = "false")
        private boolean needsReorder;
        
        @Schema(description = "Cantidad sugerida para reorden (negativo si está por encima del punto de reorden)", example = "-30")
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