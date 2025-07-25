package com.las4as.POSBackend.Sales.Interfaces.resources;

import com.las4as.POSBackend.Inventory.Application.queryServices.ProductQueryService;
import com.las4as.POSBackend.Inventory.Domain.model.aggregates.Product;
import com.las4as.POSBackend.Inventory.Interfaces.transform.ProductDTO;
import com.las4as.POSBackend.Inventory.Interfaces.transform.ProductTransformer;
import com.las4as.POSBackend.Sales.Application.commandServices.AddPaymentCommandService;
import com.las4as.POSBackend.Sales.Application.commandServices.CreateSaleCommandService;
import com.las4as.POSBackend.Sales.Application.queryServices.SaleQueryService;
import com.las4as.POSBackend.Sales.Domain.model.aggregates.Sale;
import com.las4as.POSBackend.Sales.Domain.model.commands.AddPaymentCommand;
import com.las4as.POSBackend.Sales.Domain.model.commands.CreateSaleCommand;
import com.las4as.POSBackend.Sales.Interfaces.transform.SaleDTO;
import com.las4as.POSBackend.Sales.Interfaces.transform.SaleTransformer;
import com.las4as.POSBackend.shared.interfaces.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/pos")
@RequiredArgsConstructor
@Tag(name = "POS - Sistema Punto de Venta", description = "API para gestionar las operaciones del sistema punto de venta")
public class POSResource {
    
    private final ProductQueryService productQueryService;
    private final ProductTransformer productTransformer;
    private final CreateSaleCommandService createSaleCommandService;
    private final AddPaymentCommandService addPaymentCommandService;
    private final SaleQueryService saleQueryService;
    private final SaleTransformer saleTransformer;
    
    @GetMapping("/products/search")
    @Operation(
        summary = "Buscar productos para POS", 
        description = "Busca productos por código, nombre o código de barras para agregar a una venta"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Productos encontrados exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Productos encontrados",
                    summary = "Lista de productos que coinciden con la búsqueda",
                    value = "{\n  \"success\": true,\n  \"data\": [\n    {\n      \"id\": 1,\n      \"name\": \"Coca Cola 500ml\",\n      \"code\": \"COC001\",\n      \"barcode\": \"7750182000123\",\n      \"price\": 3.50,\n      \"stock\": 100\n    }\n  ]\n}"
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProducts(
            @RequestParam @Parameter(description = "Término de búsqueda (código, nombre o código de barras)", example = "coca") String query) {
        try {
            List<Product> products = productQueryService.searchProducts(query);
            List<ProductDTO> productDTOs = products.stream()
                    .map(productTransformer::toDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(
                ApiResponse.success("Productos encontrados", "PRODUCTS_FOUND", productDTOs)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al buscar productos", "SEARCH_ERROR"));
        }
    }
    
    @PostMapping("/sales")
    @Operation(
        summary = "Crear nueva venta", 
        description = "Crea una nueva venta con los productos seleccionados y descuentos aplicados"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "Venta creada exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Venta creada",
                    summary = "Respuesta exitosa con detalles de la venta",
                    value = "{\n  \"success\": true,\n  \"message\": \"Venta creada exitosamente\",\n  \"code\": \"SALE_CREATED\",\n  \"data\": {\n    \"id\": 1,\n    \"saleNumber\": \"V20240724123456\",\n    \"status\": \"PENDING\",\n    \"total\": 5605.00,\n    \"pendingAmount\": 5605.00\n  }\n}"
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<SaleDTO>> createSale(@RequestBody CreateSaleRequest request) {
        try {
            // Convertir los items del request al formato del comando
            List<CreateSaleCommand.SaleItemData> itemsData = request.getItems().stream()
                    .map(item -> new CreateSaleCommand.SaleItemData(
                            item.getProductId(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            BigDecimal.ZERO, // discountPercentage
                            BigDecimal.ZERO, // discountAmount
                            null, // discountType
                            null  // notes
                    ))
                    .collect(Collectors.toList());
            
            CreateSaleCommand command = new CreateSaleCommand(
                    request.getCustomerId(),
                    request.getCashierId(),
                    itemsData,
                    request.getNotes()
            );
            
            Sale sale = createSaleCommandService.execute(command);
            SaleDTO saleDTO = saleTransformer.toDTO(sale);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Venta creada exitosamente", "SALE_CREATED", saleDTO));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al crear la venta", "CREATION_ERROR"));
        }
    }
    
    @PostMapping("/sales/{saleId}/payments")
    @Operation(
        summary = "Agregar pago a venta", 
        description = "Agrega un pago a una venta existente. Puede ser efectivo, tarjeta, transferencia, etc."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Pago agregado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Pago agregado",
                    summary = "Respuesta exitosa del pago",
                    value = "{\n  \"success\": true,\n  \"message\": \"Pago agregado exitosamente\",\n  \"code\": \"PAYMENT_ADDED\",\n  \"data\": {\n    \"id\": 1,\n    \"status\": \"COMPLETED\",\n    \"totalPaid\": 5605.00,\n    \"changeAmount\": 0.00\n  }\n}"
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<SaleDTO>> addPayment(
            @PathVariable @Parameter(description = "ID de la venta", example = "1") Long saleId,
            @RequestBody AddPaymentRequest request) {
        try {
            AddPaymentCommand command = new AddPaymentCommand(
                    saleId,
                    request.getPaymentMethod(),
                    request.getAmount(),
                    request.getReferenceNumber(),
                    request.getNotes()
            );
            
            Sale sale = addPaymentCommandService.execute(command);
            SaleDTO saleDTO = saleTransformer.toDTO(sale);
            
            return ResponseEntity.ok(
                ApiResponse.success("Pago agregado exitosamente", "PAYMENT_ADDED", saleDTO)
            );
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al procesar el pago", "PAYMENT_ERROR"));
        }
    }
    
    @GetMapping("/sales/{saleId}")
    @Operation(
        summary = "Obtener venta por ID", 
        description = "Obtiene los detalles completos de una venta específica incluyendo items y pagos"
    )
    public ResponseEntity<ApiResponse<SaleDTO>> getSale(@PathVariable Long saleId) {
        return saleQueryService.findById(saleId)
                .map(sale -> {
                    SaleDTO saleDTO = saleTransformer.toDTO(sale);
                    return ResponseEntity.ok(
                        ApiResponse.success("Venta encontrada", "SALE_FOUND", saleDTO)
                    );
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Venta no encontrada", "SALE_NOT_FOUND")));
    }
    
    @GetMapping("/sales")
    @Operation(
        summary = "Listar todas las ventas", 
        description = "Obtiene una lista paginada de todas las ventas con filtros opcionales"
    )
    public ResponseEntity<ApiResponse<List<SaleDTO>>> getAllSales(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            List<Sale> sales;
            
            if (status != null) {
                Sale.SaleStatus saleStatus = Sale.SaleStatus.valueOf(status.toUpperCase());
                sales = saleQueryService.findByStatus(saleStatus);
            } else if (customerId != null) {
                sales = saleQueryService.findByCustomerId(customerId);
            } else if (startDate != null && endDate != null) {
                LocalDateTime start = startDate.atStartOfDay();
                LocalDateTime end = endDate.atTime(23, 59, 59);
                sales = saleQueryService.findBySaleDateBetween(start, end);
            } else {
                sales = saleQueryService.findTodaySales();
            }
            
            List<SaleDTO> saleDTOs = sales.stream()
                    .map(saleTransformer::toDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(
                ApiResponse.success("Ventas obtenidas exitosamente", "SALES_FOUND", saleDTOs)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al obtener las ventas", "QUERY_ERROR"));
        }
    }
    
    @PutMapping("/sales/{saleId}/cancel")
    @Operation(summary = "Cancelar venta", description = "Cancela una venta pendiente")
    public ResponseEntity<ApiResponse<SaleDTO>> cancelSale(@PathVariable Long saleId) {
        try {
            Sale sale = saleQueryService.findById(saleId)
                    .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
            
            sale.cancel();
            SaleDTO saleDTO = saleTransformer.toDTO(sale);
            
            return ResponseEntity.ok(
                ApiResponse.success("Venta cancelada exitosamente", "SALE_CANCELLED", saleDTO)
            );
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "CANCELLATION_ERROR"));
        }
    }
    
    @PostMapping("/cash-register/open")
    @Operation(summary = "Abrir caja registradora", description = "Abre la caja registradora para iniciar el turno")
    public ResponseEntity<ApiResponse<Map<String, Object>>> openCashRegister(
            @RequestParam BigDecimal initialAmount) {
        try {
            String sessionId = "CR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            
            Map<String, Object> cashRegisterData = new HashMap<>();
            cashRegisterData.put("sessionId", sessionId);
            cashRegisterData.put("cashierId", 1L);
            cashRegisterData.put("openTime", LocalDateTime.now());
            cashRegisterData.put("initialAmount", initialAmount);
            
            return ResponseEntity.ok(
                ApiResponse.success("Caja registradora abierta exitosamente", "CASH_REGISTER_OPENED", cashRegisterData)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al abrir la caja registradora", "CASH_REGISTER_ERROR"));
        }
    }
    
    // DTOs internos
    @Schema(description = "Request para crear una nueva venta")
    public static class CreateSaleRequest {
        @Schema(description = "ID del cliente", example = "1")
        private Long customerId;
        
        @Schema(description = "ID del cajero", example = "1", required = true)
        private Long cashierId;
        
        @Schema(description = "Lista de productos en la venta", required = true)
        private List<SaleItemRequest> items;
        
        @Schema(description = "Tipo de descuento", example = "PERCENTAGE")
        private String discountType;
        
        @Schema(description = "Valor del descuento", example = "10.00")
        private BigDecimal discountValue;
        
        @Schema(description = "Notas adicionales", example = "Venta al por mayor")
        private String notes;
        
        // Getters y setters
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        
        public Long getCashierId() { return cashierId; }
        public void setCashierId(Long cashierId) { this.cashierId = cashierId; }
        
        public List<SaleItemRequest> getItems() { return items; }
        public void setItems(List<SaleItemRequest> items) { this.items = items; }
        
        public String getDiscountType() { return discountType; }
        public void setDiscountType(String discountType) { this.discountType = discountType; }
        
        public BigDecimal getDiscountValue() { return discountValue; }
        public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
    
    @Schema(description = "Item de venta en el request")
    public static class SaleItemRequest {
        @Schema(description = "ID del producto", example = "1", required = true)
        private Long productId;
        
        @Schema(description = "Cantidad", example = "2", required = true)
        private Integer quantity;
        
        @Schema(description = "Precio unitario", example = "25.50")
        private BigDecimal unitPrice;
        
        // Getters y setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }
    
    @Schema(description = "Request para agregar un pago")
    public static class AddPaymentRequest {
        @Schema(description = "Método de pago", example = "CASH", required = true)
        private String paymentMethod;
        
        @Schema(description = "Monto del pago", example = "3000.00", required = true)
        private BigDecimal amount;
        
        @Schema(description = "Número de referencia del pago")
        private String referenceNumber;
        
        @Schema(description = "Notas adicionales del pago")
        private String notes;
        
        // Getters y setters
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
