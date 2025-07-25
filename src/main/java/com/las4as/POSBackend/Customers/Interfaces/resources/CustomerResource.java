package com.las4as.POSBackend.Customers.Interfaces.resources;

import com.las4as.POSBackend.Customers.Application.commandServices.CreateCustomerCommandService;
import com.las4as.POSBackend.Customers.Application.queryServices.CustomerQueryService;
import com.las4as.POSBackend.Customers.Domain.model.aggregates.Customer;
import com.las4as.POSBackend.shared.interfaces.rest.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Gestión de Clientes", description = "Endpoints para la gestión de clientes del sistema POS")
public class CustomerResource {
    
    private final CustomerQueryService customerQueryService;
    private final CreateCustomerCommandService createCustomerCommandService;
    
    @GetMapping("/search")
    @Operation(
        summary = "Buscar clientes", 
        description = "Busca clientes por nombre, apellido, razón social o número de documento para seleccionar durante una venta",
        parameters = @Parameter(
            name = "query",
            description = "Término de búsqueda (nombre, apellido, empresa o documento)",
            example = "Juan",
            required = true
        )
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Clientes encontrados exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Clientes encontrados",
                    summary = "Lista de clientes que coinciden con la búsqueda",
                    value = "{\n  \"success\": true,\n  \"data\": [\n    {\n      \"id\": 1,\n      \"documentNumber\": \"12345678\",\n      \"documentType\": \"DNI\",\n      \"fullName\": \"Juan Pérez\",\n      \"email\": \"juan@email.com\",\n      \"phone\": \"987654321\"\n    }\n  ]\n}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Parámetro de búsqueda inválido"
        )
    })
    public ResponseEntity<ApiResponse<List<CustomerSummaryDTO>>> searchCustomers(@RequestParam String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("El término de búsqueda es requerido", "SEARCH_TERM_REQUIRED"));
            }
            
            List<Customer> customers = customerQueryService.searchActiveCustomers(query.trim());
            List<CustomerSummaryDTO> customerDTOs = customers.stream()
                    .map(this::toCustomerSummaryDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(
                ApiResponse.success("Clientes encontrados", "CUSTOMERS_FOUND", customerDTOs)
            );
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al buscar clientes", "SEARCH_ERROR"));
        }
    }
    
    @GetMapping("/{customerId}")
    @Operation(
        summary = "Obtener cliente por ID", 
        description = "Obtiene los detalles completos de un cliente específico",
        parameters = @Parameter(
            name = "customerId",
            description = "ID del cliente a consultar",
            example = "1",
            required = true
        )
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Cliente encontrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Cliente encontrado",
                    summary = "Información completa del cliente",
                    value = "{\n  \"success\": true,\n  \"data\": {\n    \"id\": 1,\n    \"documentNumber\": \"12345678\",\n    \"documentType\": \"DNI\",\n    \"firstName\": \"Juan\",\n    \"lastName\": \"Pérez\",\n    \"fullName\": \"Juan Pérez\",\n    \"email\": \"juan@email.com\",\n    \"phone\": \"987654321\",\n    \"address\": \"Av. Principal 123\",\n    \"city\": \"Lima\"\n  }\n}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Cliente no encontrado"
        )
    })
    public ResponseEntity<ApiResponse<CustomerDetailDTO>> getCustomer(@PathVariable Long customerId) {
        return customerQueryService.findById(customerId)
                .map(customer -> {
                    CustomerDetailDTO customerDTO = toCustomerDetailDTO(customer);
                    return ResponseEntity.ok(
                        ApiResponse.success("Cliente encontrado", "CUSTOMER_FOUND", customerDTO)
                    );
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Cliente no encontrado", "CUSTOMER_NOT_FOUND")));
    }
    
    @PostMapping
    @Operation(
        summary = "Crear nuevo cliente", 
        description = "Registra un nuevo cliente durante el proceso de venta o de forma independiente",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del nuevo cliente",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreateCustomerRequest.class),
                examples = {
                    @ExampleObject(
                        name = "Cliente persona natural",
                        summary = "Registro de persona con DNI",
                        value = "{\n  \"documentNumber\": \"12345678\",\n  \"firstName\": \"Juan\",\n  \"lastName\": \"Pérez\",\n  \"email\": \"juan@email.com\",\n  \"phone\": \"987654321\",\n  \"address\": \"Av. Principal 123\",\n  \"city\": \"Lima\",\n  \"state\": \"Lima\",\n  \"country\": \"Perú\"\n}"
                    ),
                    @ExampleObject(
                        name = "Cliente empresa",
                        summary = "Registro de empresa con RUC",
                        value = "{\n  \"documentNumber\": \"20123456789\",\n  \"companyName\": \"Empresa SAC\",\n  \"email\": \"contacto@empresa.com\",\n  \"phone\": \"987654321\",\n  \"address\": \"Av. Comercial 456\",\n  \"city\": \"Lima\",\n  \"state\": \"Lima\",\n  \"country\": \"Perú\"\n}"
                    )
                }
            )
        )
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "Cliente creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Cliente creado",
                    summary = "Respuesta exitosa con datos del cliente",
                    value = "{\n  \"success\": true,\n  \"message\": \"Cliente creado exitosamente\",\n  \"code\": \"CUSTOMER_CREATED\",\n  \"data\": {\n    \"id\": 1,\n    \"documentNumber\": \"12345678\",\n    \"fullName\": \"Juan Pérez\",\n    \"documentType\": \"DNI\"\n  }\n}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Datos del cliente inválidos",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error de validación",
                    value = "{\n  \"success\": false,\n  \"message\": \"El número de documento ya está registrado\",\n  \"code\": \"DOCUMENT_ALREADY_EXISTS\"\n}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", 
            description = "Cliente ya existe",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Cliente duplicado",
                    value = "{\n  \"success\": false,\n  \"message\": \"Ya existe un cliente con este número de documento\",\n  \"code\": \"CUSTOMER_ALREADY_EXISTS\"\n}"
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<CustomerSummaryDTO>> createCustomer(@RequestBody CreateCustomerRequest request) {
        try {
            // Validaciones básicas
            if (request.getDocumentNumber() == null || request.getDocumentNumber().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("El número de documento es requerido", "DOCUMENT_REQUIRED"));
            }
            
            Customer customer;
            
            // Determinar si es persona o empresa basado en la longitud del documento
            if (request.getDocumentNumber().trim().length() == 8) {
                // DNI - Persona natural
                if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("El nombre es requerido para DNI", "FIRST_NAME_REQUIRED"));
                }
                if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("El apellido es requerido para DNI", "LAST_NAME_REQUIRED"));
                }
                
                customer = createCustomerCommandService.createPersonCustomer(
                    request.getDocumentNumber().trim(),
                    request.getFirstName().trim(),
                    request.getLastName().trim(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getAddress(),
                    request.getCity(),
                    request.getState(),
                    request.getPostalCode(),
                    request.getCountry(),
                    request.getNotes()
                );
            } else {
                // RUC - Empresa
                if (request.getCompanyName() == null || request.getCompanyName().trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("La razón social es requerida para RUC", "COMPANY_NAME_REQUIRED"));
                }
                
                customer = createCustomerCommandService.createCompanyCustomer(
                    request.getDocumentNumber().trim(),
                    request.getCompanyName().trim(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getAddress(),
                    request.getCity(),
                    request.getState(),
                    request.getPostalCode(),
                    request.getCountry(),
                    request.getNotes()
                );
            }
            
            CustomerSummaryDTO customerDTO = toCustomerSummaryDTO(customer);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Cliente creado exitosamente", "CUSTOMER_CREATED", customerDTO));
                    
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("ya existe")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error(e.getMessage(), "CUSTOMER_ALREADY_EXISTS"));
            }
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), "VALIDATION_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error al crear el cliente", "CREATION_ERROR"));
        }
    }
    
    private CustomerSummaryDTO toCustomerSummaryDTO(Customer customer) {
        CustomerSummaryDTO dto = new CustomerSummaryDTO();
        dto.setId(customer.getId());
        dto.setDocumentNumber(customer.getDocumentNumber().toString());
        dto.setDocumentType(customer.getDocumentType().name());
        dto.setFullName(customer.getFullName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        return dto;
    }
    
    private CustomerDetailDTO toCustomerDetailDTO(Customer customer) {
        CustomerDetailDTO dto = new CustomerDetailDTO();
        dto.setId(customer.getId());
        dto.setDocumentNumber(customer.getDocumentNumber().toString());
        dto.setDocumentType(customer.getDocumentType().name());
        dto.setFirstName(customer.getFirstName());
        dto.setLastName(customer.getLastName());
        dto.setCompanyName(customer.getCompanyName());
        dto.setFullName(customer.getFullName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        dto.setCity(customer.getCity());
        dto.setState(customer.getState());
        dto.setPostalCode(customer.getPostalCode());
        dto.setCountry(customer.getCountry());
        dto.setActive(customer.isActive());
        dto.setNotes(customer.getNotes());
        return dto;
    }
    
    // DTOs
    @Schema(description = "Información resumida del cliente")
    public static class CustomerSummaryDTO {
        @Schema(description = "ID del cliente", example = "1")
        private Long id;
        
        @Schema(description = "Número de documento", example = "12345678")
        private String documentNumber;
        
        @Schema(description = "Tipo de documento", example = "DNI")
        private String documentType;
        
        @Schema(description = "Nombre completo o razón social", example = "Juan Pérez")
        private String fullName;
        
        @Schema(description = "Email del cliente", example = "juan@email.com")
        private String email;
        
        @Schema(description = "Teléfono del cliente", example = "987654321")
        private String phone;
        
        // Getters y setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getDocumentNumber() { return documentNumber; }
        public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
        
        public String getDocumentType() { return documentType; }
        public void setDocumentType(String documentType) { this.documentType = documentType; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }
    
    @Schema(description = "Información detallada del cliente")
    public static class CustomerDetailDTO extends CustomerSummaryDTO {
        @Schema(description = "Nombre del cliente", example = "Juan")
        private String firstName;
        
        @Schema(description = "Apellido del cliente", example = "Pérez")
        private String lastName;
        
        @Schema(description = "Razón social (para empresas)", example = "Empresa SAC")
        private String companyName;
        
        @Schema(description = "Dirección del cliente", example = "Av. Principal 123")
        private String address;
        
        @Schema(description = "Ciudad", example = "Lima")
        private String city;
        
        @Schema(description = "Estado/Región", example = "Lima")
        private String state;
        
        @Schema(description = "Código postal", example = "15001")
        private String postalCode;
        
        @Schema(description = "País", example = "Perú")
        private String country;
        
        @Schema(description = "Estado activo del cliente", example = "true")
        private boolean active;
        
        @Schema(description = "Notas adicionales", example = "Cliente preferencial")
        private String notes;
        
        // Getters y setters adicionales
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
    
    @Schema(description = "Datos requeridos para crear un nuevo cliente")
    public static class CreateCustomerRequest {
        @Schema(description = "Número de documento (8 dígitos para DNI, 11 para RUC)", example = "12345678", required = true)
        private String documentNumber;
        
        @Schema(description = "Nombre del cliente (requerido para DNI)", example = "Juan")
        private String firstName;
        
        @Schema(description = "Apellido del cliente (requerido para DNI)", example = "Pérez")
        private String lastName;
        
        @Schema(description = "Razón social (requerido para RUC)", example = "Empresa SAC")
        private String companyName;
        
        @Schema(description = "Email del cliente", example = "juan@email.com")
        private String email;
        
        @Schema(description = "Teléfono del cliente", example = "987654321")
        private String phone;
        
        @Schema(description = "Dirección del cliente", example = "Av. Principal 123")
        private String address;
        
        @Schema(description = "Ciudad", example = "Lima")
        private String city;
        
        @Schema(description = "Estado/Región", example = "Lima")
        private String state;
        
        @Schema(description = "Código postal", example = "15001")
        private String postalCode;
        
        @Schema(description = "País", example = "Perú")
        private String country;
        
        @Schema(description = "Notas adicionales", example = "Cliente referido")
        private String notes;
        
        // Getters y setters
        public String getDocumentNumber() { return documentNumber; }
        public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }
}
