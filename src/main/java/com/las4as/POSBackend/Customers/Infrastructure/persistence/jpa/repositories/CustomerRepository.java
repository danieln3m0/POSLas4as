package com.las4as.POSBackend.Customers.Infrastructure.persistence.jpa.repositories;

import com.las4as.POSBackend.Customers.Domain.model.aggregates.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByDocumentNumberValue(String documentNumber);
    
    boolean existsByDocumentNumberValue(String documentNumber);
    
    @Query("SELECT c FROM Customer c WHERE c.isActive = true")
    List<Customer> findActiveCustomers();
    
    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND " +
           "(LOWER(c.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "c.documentNumber.value LIKE CONCAT('%', :searchTerm, '%'))")
    List<Customer> searchActiveCustomers(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND c.documentType = :documentType")
    List<Customer> findByDocumentType(@Param("documentType") Customer.DocumentType documentType);
    
    @Query("SELECT c FROM Customer c WHERE c.isActive = true AND " +
           "(c.email LIKE LOWER(CONCAT('%', :email, '%')))")
    List<Customer> findByEmail(@Param("email") String email);
}
