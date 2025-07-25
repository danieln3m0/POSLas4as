package com.las4as.POSBackend.Sales.Application.commandServices;

import com.las4as.POSBackend.Customers.Domain.model.aggregates.Customer;
import com.las4as.POSBackend.Customers.Infrastructure.persistence.jpa.repositories.CustomerRepository;
import com.las4as.POSBackend.IAM.Domain.model.aggregates.User;
import com.las4as.POSBackend.IAM.Infrastructure.persistence.jpa.repositories.UserRepository;
import com.las4as.POSBackend.Inventory.Domain.model.aggregates.Product;
import com.las4as.POSBackend.Inventory.Infrastructure.persistence.jpa.repositories.ProductRepository;
import com.las4as.POSBackend.Sales.Domain.model.aggregates.Sale;
import com.las4as.POSBackend.Sales.Domain.model.commands.CreateSaleCommand;
import com.las4as.POSBackend.Sales.Domain.model.entities.SaleItem;
import com.las4as.POSBackend.Sales.Domain.model.valueobjects.Discount;
import com.las4as.POSBackend.Sales.Infrastructure.persistence.jpa.repositories.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateSaleCommandService {
    
    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    
    public Sale execute(CreateSaleCommand command) {
        // Validar cashier
        User cashier = userRepository.findById(command.getCashierId())
                .orElseThrow(() -> new IllegalArgumentException("Cajero no encontrado"));
        
        // Validar customer (opcional)
        Customer customer = null;
        if (command.getCustomerId() != null) {
            customer = customerRepository.findById(command.getCustomerId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        }
        
        // Generar número de venta único
        String saleNumber = generateSaleNumber();
        
        // Crear la venta
        Sale sale = new Sale(saleNumber, cashier, customer, command.getNotes());
        
        // Agregar items
        for (CreateSaleCommand.SaleItemData itemData : command.getItems()) {
            Product product = productRepository.findById(itemData.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + itemData.getProductId()));
            
            // Crear descuento si aplica
            Discount discount = createDiscount(itemData);
            
            // Crear item de venta
            SaleItem saleItem = new SaleItem(
                product, 
                itemData.getQuantity(), 
                itemData.getUnitPrice(), 
                discount, 
                itemData.getNotes()
            );
            
            sale.addItem(saleItem);
        }
        
        return saleRepository.save(sale);
    }
    
    private String generateSaleNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String baseNumber = "V" + timestamp;
        
        // Verificar que sea único
        String saleNumber = baseNumber;
        int counter = 1;
        while (saleRepository.existsBySaleNumber(saleNumber)) {
            saleNumber = baseNumber + String.format("%03d", counter);
            counter++;
        }
        
        return saleNumber;
    }
    
    private Discount createDiscount(CreateSaleCommand.SaleItemData itemData) {
        if (itemData.getDiscountType() == null || itemData.getDiscountType().isEmpty()) {
            return Discount.noDiscount();
        }
        
        switch (itemData.getDiscountType()) {
            case "PERCENTAGE":
                return new Discount(itemData.getDiscountPercentage());
            case "FIXED_AMOUNT":
                return new Discount(itemData.getDiscountAmount(), "FIXED_AMOUNT");
            default:
                return Discount.noDiscount();
        }
    }
}
