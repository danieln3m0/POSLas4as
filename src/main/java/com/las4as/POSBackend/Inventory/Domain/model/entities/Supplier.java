package com.las4as.POSBackend.Inventory.Domain.model.entities;

import com.las4as.POSBackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "suppliers")
@Getter
@NoArgsConstructor
public class Supplier extends AuditableModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 200)
    private String contactPerson;
    
    @Column(length = 100)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 200)
    private String address;
    
    @Column(length = 100)
    private String city;
    
    @Column(length = 50)
    private String state;
    
    @Column(length = 20)
    private String postalCode;
    
    @Column(length = 50)
    private String country;
    
    @Column(name = "tax_id", length = 50)
    private String taxId;
    
    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(length = 500)
    private String notes;
    
    public Supplier(String name, String contactPerson, String email, String phone, 
                   String address, String city, String state, String postalCode, 
                   String country, String taxId, String paymentTerms, String notes) {
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.taxId = taxId;
        this.paymentTerms = paymentTerms;
        this.notes = notes;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public void deactivate() {
        this.isActive = false;
    }
    
    public void updateContactInfo(String contactPerson, String email, String phone) {
        this.contactPerson = contactPerson;
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
    
    public String getFullAddress() {
        return String.format("%s, %s, %s %s, %s", 
            address, city, state, postalCode, country);
    }
} 