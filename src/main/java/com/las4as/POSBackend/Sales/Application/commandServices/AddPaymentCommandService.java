package com.las4as.POSBackend.Sales.Application.commandServices;

import com.las4as.POSBackend.Sales.Domain.model.aggregates.Sale;
import com.las4as.POSBackend.Sales.Domain.model.commands.AddPaymentCommand;
import com.las4as.POSBackend.Sales.Domain.model.entities.Payment;
import com.las4as.POSBackend.Sales.Domain.model.valueobjects.PaymentAmount;
import com.las4as.POSBackend.Sales.Infrastructure.persistence.jpa.repositories.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AddPaymentCommandService {
    
    private final SaleRepository saleRepository;
    
    public Sale execute(AddPaymentCommand command) {
        Sale sale = saleRepository.findById(command.getSaleId())
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
        
        // Validar método de pago
        Payment.PaymentMethod paymentMethod;
        try {
            paymentMethod = Payment.PaymentMethod.valueOf(command.getPaymentMethod());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Método de pago inválido: " + command.getPaymentMethod());
        }
        
        // Crear el pago
        PaymentAmount amount = new PaymentAmount(command.getAmount());
        Payment payment = new Payment(paymentMethod, amount, command.getReferenceNumber(), command.getNotes());
        
        // Agregar el pago a la venta
        sale.addPayment(payment);
        
        return saleRepository.save(sale);
    }
}
