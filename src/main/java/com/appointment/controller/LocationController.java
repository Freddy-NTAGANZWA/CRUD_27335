package com.appointment.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appointment.model.Location;
import com.appointment.repository.LocationRepository;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    
    private final LocationRepository locationRepository;

    public LocationController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveLocation(@RequestBody Location location) {
        // For saving parent locations (Province) - parent should be null
        if (location.getParent() != null) {
            return new ResponseEntity<>("Parent location should not have a parent. Use /saveWithParent for child locations.", HttpStatus.BAD_REQUEST);
        }
        
        // Check if location with same code already exists
        if (location.getCode() != null && locationRepository.existsByCode(location.getCode())) {
            return new ResponseEntity<>("Location with code '" + location.getCode() + "' already exists", HttpStatus.CONFLICT);
        }
        
        Location saved = locationRepository.save(location);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PostMapping(value = "/saveWithParent", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveLocationWithParent(
            @RequestParam(required = false) String parentCode,
            @RequestParam(required = false) Long parentId,
            @RequestBody Location location) {
        
        try {
            Location parent = null;
            
            // Priority 1: Use parentId from query parameter
            if (parentId != null) {
                Optional<Location> parentOptional = locationRepository.findById(parentId);
                if (!parentOptional.isPresent()) {
                    return new ResponseEntity<>("Parent location with ID '" + parentId + "' not found", HttpStatus.NOT_FOUND);
                }
                parent = parentOptional.get();
            }
            // Priority 2: Use parentId from request body
            else if (location.getParentId() != null) {
                Optional<Location> parentOptional = locationRepository.findById(location.getParentId());
                if (!parentOptional.isPresent()) {
                    return new ResponseEntity<>("Parent location with ID '" + location.getParentId() + "' not found", HttpStatus.NOT_FOUND);
                }
                parent = parentOptional.get();
            }
            // Priority 3: Use parentCode if provided
            else if (parentCode != null && !parentCode.trim().isEmpty()) {
                Optional<Location> parentOptional = locationRepository.findByCode(parentCode);
                if (!parentOptional.isPresent()) {
                    return new ResponseEntity<>("Parent location with code '" + parentCode + "' not found", HttpStatus.NOT_FOUND);
                }
                parent = parentOptional.get();
            }
            // Priority 4: Use parent object from body if provided
            else if (location.getParent() != null && location.getParent().getId() != null) {
                Optional<Location> parentOptional = locationRepository.findById(location.getParent().getId());
                if (!parentOptional.isPresent()) {
                    return new ResponseEntity<>("Parent location with ID '" + location.getParent().getId() + "' not found", HttpStatus.NOT_FOUND);
                }
                parent = parentOptional.get();
            }
            // No parent provided
            else {
                return new ResponseEntity<>("Parent ID, parent code, or parent object is required for child locations", HttpStatus.BAD_REQUEST);
            }
            
            // Validate location data
            if (location.getName() == null || location.getName().trim().isEmpty()) {
                return new ResponseEntity<>("Location name is required", HttpStatus.BAD_REQUEST);
            }
            
            if (location.getType() == null) {
                return new ResponseEntity<>("Location type is required", HttpStatus.BAD_REQUEST);
            }
            
            // Set the parent
            location.setParent(parent);
            
            // Check if location with same code already exists
            if (location.getCode() != null && locationRepository.existsByCode(location.getCode())) {
                return new ResponseEntity<>("Location with code '" + location.getCode() + "' already exists", HttpStatus.CONFLICT);
            }
            
            Location saved = locationRepository.save(location);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error saving location: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Location>> getAllLocations() {
        return new ResponseEntity<>(locationRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getLocationById(@PathVariable Long id) {
        Optional<Location> location = locationRepository.findById(id);
        if (location.isPresent()) {
            return new ResponseEntity<>(location.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Location not found", HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping(value = "/provinces", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Location>> getProvinces() {
        return new ResponseEntity<>(locationRepository.findByType(com.appointment.model.LocationType.PROVINCE), HttpStatus.OK);
    }
    
    @GetMapping(value = "/byParent/{parentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Location>> getLocationsByParent(@PathVariable Long parentId) {
        return new ResponseEntity<>(locationRepository.findByParentId(parentId), HttpStatus.OK);
    }
}
