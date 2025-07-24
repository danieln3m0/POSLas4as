package com.las4as.POSBackend.Inventory.Infrastructure.persistence.jpa.repositories;

import com.las4as.POSBackend.Inventory.Domain.model.entities.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    Optional<Location> findByName(String name);
    
    List<Location> findByIsActiveTrue();
    
    List<Location> findByLocationType(Location.LocationType locationType);
    
    List<Location> findByCity(String city);
    
    List<Location> findByState(String state);
    
    boolean existsByName(String name);
} 