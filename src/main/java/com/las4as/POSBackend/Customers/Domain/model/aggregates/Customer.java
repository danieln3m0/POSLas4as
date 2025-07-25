package com.las4as.POSBackend.Customers.Domain.model.aggregates;

import com.las4as.POSBackend.Customers.Domain.model.valueobjects.DocumentNumber;
import com.las4as.POSBackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor
public class Customer extends AuditableAbstractAggregateRoot<Customer> {
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "document_number", nullable = false, unique = true, length = 11))
    })
    private DocumentNumber documentNumber;
    
    @Column(name = "document_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private DocumentType documentType;
    
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @Column(name = "company_name", length = 200)
    private String companyName;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "address", length = 200)
    private String address;
    
    @Column(name = "city", length = 100)
    private String city;
    
    @Column(name = "state", length = 50)
    private String state;
    
    @Column(name = "postal_code", length = 20)
    private String postalCode;
    
    @Column(name = "country", length = 50)
    private String country;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "notes", length = 500)
    private String notes;
    
    public enum DocumentType {
        DNI, RUC
    }
    
    public Customer(DocumentNumber documentNumber, String firstName, String lastName, 
                   String email, String phone, String address, String city, 
                   String state, String postalCode, String country, String notes) {
        this.documentNumber = documentNumber;
        this.documentType = documentNumber.isDNI() ? DocumentType.DNI : DocumentType.RUC;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.notes = notes;
    }
    
    public Customer(DocumentNumber documentNumber, String companyName, String email, 
                   String phone, String address, String city, String state, 
                   String postalCode, String country, String notes) {
        this.documentNumber = documentNumber;
        this.documentType = DocumentType.RUC;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.notes = notes;
    }
    
    public void updatePersonalInfo(String firstName, String lastName, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }
    
    public void updateCompanyInfo(String companyName, String email, String phone) {
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
    }
    
    public void updateAddress(String address, String city, String state, String postalCode, String country) {
        this.address = address;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public String getFullName() {
        if (companyName != null && !companyName.trim().isEmpty()) {
            return companyName;
        }
        return firstName + " " + lastName;
    }
    
    public String getFullAddress() {
        if (address == null || address.trim().isEmpty()) {
            return "";
        }
        return String.format("%s, %s, %s %s, %s", 
            address, city, state, postalCode, country);
    }
    
    public boolean isCompany() {
        return documentType == DocumentType.RUC;
    }
    
    public boolean isPerson() {
        return documentType == DocumentType.DNI;
    }
}
