package com.las4as.POSBackend.Customers.Application.queryServices;

import com.las4as.POSBackend.Customers.Domain.model.aggregates.Customer;
import com.las4as.POSBackend.Customers.Infrastructure.persistence.jpa.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerQueryService {
    
    private final CustomerRepository customerRepository;
    
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }
    
    public Optional<Customer> findByDocumentNumber(String documentNumber) {
        return customerRepository.findByDocumentNumberValue(documentNumber);
    }
    
    public List<Customer> findActiveCustomers() {
        return customerRepository.findActiveCustomers();
    }
    
    public List<Customer> searchActiveCustomers(String searchTerm) {
        return customerRepository.searchActiveCustomers(searchTerm);
    }
    
    public List<Customer> findByDocumentType(Customer.DocumentType documentType) {
        return customerRepository.findByDocumentType(documentType);
    }
    
    public List<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }
    
    public boolean existsByDocumentNumber(String documentNumber) {
        return customerRepository.existsByDocumentNumberValue(documentNumber);
    }
}
