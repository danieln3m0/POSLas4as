package com.las4as.POSBackend.Customers.Application.commandServices;

import com.las4as.POSBackend.Customers.Domain.model.aggregates.Customer;
import com.las4as.POSBackend.Customers.Domain.model.valueobjects.DocumentNumber;
import com.las4as.POSBackend.Customers.Infrastructure.persistence.jpa.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateCustomerCommandService {
    
    private final CustomerRepository customerRepository;
    
    public Customer createPersonCustomer(String documentNumber, String firstName, String lastName, 
                                       String email, String phone, String address, String city, 
                                       String state, String postalCode, String country, String notes) {
        
        // Validar que el documento no exista
        if (customerRepository.existsByDocumentNumberValue(documentNumber)) {
            throw new IllegalArgumentException("Ya existe un cliente con este número de documento");
        }
        
        DocumentNumber docNumber = new DocumentNumber(documentNumber);
        
        Customer customer = new Customer(docNumber, firstName, lastName, email, phone, 
                                       address, city, state, postalCode, country, notes);
        
        return customerRepository.save(customer);
    }
    
    public Customer createCompanyCustomer(String documentNumber, String companyName, String email, 
                                        String phone, String address, String city, String state, 
                                        String postalCode, String country, String notes) {
        
        // Validar que el documento no exista
        if (customerRepository.existsByDocumentNumberValue(documentNumber)) {
            throw new IllegalArgumentException("Ya existe un cliente con este número de documento");
        }
        
        DocumentNumber docNumber = new DocumentNumber(documentNumber);
        
        Customer customer = new Customer(docNumber, companyName, email, phone, 
                                       address, city, state, postalCode, country, notes);
        
        return customerRepository.save(customer);
    }
}
