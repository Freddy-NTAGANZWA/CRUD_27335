package com.appointment.controller;

import com.appointment.model.Service;
import com.appointment.repository.ServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/services")
public class ServiceController {
    
    private final ServiceRepository serviceRepository;

    public ServiceController(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createService(@RequestBody Service service) {
        try {
            // Validate required fields
            if (service.getName() == null || service.getName().trim().isEmpty()) {
                return new ResponseEntity<>("Service name is required", HttpStatus.BAD_REQUEST);
            }
            
            // Check if service name already exists
            if (serviceRepository.existsByName(service.getName())) {
                return new ResponseEntity<>("Service with name '" + service.getName() + "' already exists", HttpStatus.CONFLICT);
            }
            
            Service saved = serviceRepository.save(service);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error creating service: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Service>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("DESC") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Service> services = serviceRepository.findAll(pageable);
        return new ResponseEntity<>(services, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getServiceById(@PathVariable Long id) {
        Optional<Service> service = serviceRepository.findById(id);
        if (service.isPresent()) {
            return new ResponseEntity<>(service.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Service not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/exists/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> checkNameExists(@PathVariable String name) {
        return new ResponseEntity<>(serviceRepository.existsByName(name), HttpStatus.OK);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateService(@PathVariable Long id, @RequestBody Service service) {
        try {
            Optional<Service> existingOptional = serviceRepository.findById(id);
            if (!existingOptional.isPresent()) {
                return new ResponseEntity<>("Service not found", HttpStatus.NOT_FOUND);
            }
            
            Service existing = existingOptional.get();
            
            // Update fields
            if (service.getName() != null && !service.getName().trim().isEmpty()) {
                existing.setName(service.getName());
            }
            if (service.getDescription() != null) {
                existing.setDescription(service.getDescription());
            }
            if (service.getPrice() != null) {
                existing.setPrice(service.getPrice());
            }
            
            Service updated = serviceRepository.save(existing);
            return new ResponseEntity<>(updated, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error updating service: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        try {
            Optional<Service> service = serviceRepository.findById(id);
            if (!service.isPresent()) {
                return new ResponseEntity<>("Service not found", HttpStatus.NOT_FOUND);
            }
            
            serviceRepository.deleteById(id);
            return new ResponseEntity<>("Service deleted successfully", HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error deleting service: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
