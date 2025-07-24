package com.las4as.POSBackend.Inventory.Domain.model.entities;

import com.las4as.POSBackend.shared.domain.model.entities.AuditableModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locations")
@Getter
@NoArgsConstructor
public class Location extends AuditableModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
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
    
    @Column(name = "location_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private LocationType locationType;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(length = 500)
    private String description;
    
    public enum LocationType {
        WAREHOUSE,      // Almacén
        STORE,          // Tienda/Punto de venta
        DISTRIBUTION    // Centro de distribución
    }
    
    public Location(String name, String address, String city, String state, 
                   String postalCode, String country, LocationType locationType, String description) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.locationType = locationType;
        this.description = description;
    }
    
    public void activate() {
        this.isActive = true;
    }
    
    public void deactivate() {
        this.isActive = false;
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