package com.appointment.controller;

import com.appointment.model.Appointment;
import com.appointment.model.User;
import com.appointment.model.Service;
import com.appointment.repository.AppointmentRepository;
import com.appointment.repository.UserRepository;
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
@RequestMapping("/api/appointments")
public class AppointmentController {
    
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;

    public AppointmentController(AppointmentRepository appointmentRepository,
                                UserRepository userRepository,
                                ServiceRepository serviceRepository) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
    }

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createAppointment(@RequestBody Appointment appointment) {
        try {
            // Validate required fields
            if (appointment.getAppointmentTime() == null) {
                return new ResponseEntity<>("Appointment time is required", HttpStatus.BAD_REQUEST);
            }
            
            // Handle userId
            if (appointment.getUserId() != null) {
                Optional<User> userOptional = userRepository.findById(appointment.getUserId());
                if (!userOptional.isPresent()) {
                    return new ResponseEntity<>("User with ID '" + appointment.getUserId() + "' not found", HttpStatus.NOT_FOUND);
                }
                appointment.setUser(userOptional.get());
            } else if (appointment.getUser() == null || appointment.getUser().getId() == null) {
                return new ResponseEntity<>("User ID is required", HttpStatus.BAD_REQUEST);
            } else {
                Optional<User> userOptional = userRepository.findById(appointment.getUser().getId());
                if (!userOptional.isPresent()) {
                    return new ResponseEntity<>("User with ID '" + appointment.getUser().getId() + "' not found", HttpStatus.NOT_FOUND);
                }
                appointment.setUser(userOptional.get());
            }
            
            // Handle specialistId
            if (appointment.getSpecialistId() != null) {
                Optional<User> specialistOptional = userRepository.findById(appointment.getSpecialistId());
                if (!specialistOptional.isPresent()) {
                    return new ResponseEntity<>("Specialist with ID '" + appointment.getSpecialistId() + "' not found", HttpStatus.NOT_FOUND);
                }
                appointment.setSpecialist(specialistOptional.get());
            } else if (appointment.getSpecialist() != null && appointment.getSpecialist().getId() != null) {
                Optional<User> specialistOptional = userRepository.findById(appointment.getSpecialist().getId());
                if (!specialistOptional.isPresent()) {
                    return new ResponseEntity<>("Specialist with ID '" + appointment.getSpecialist().getId() + "' not found", HttpStatus.NOT_FOUND);
                }
                appointment.setSpecialist(specialistOptional.get());
            }
            
            // Handle serviceId
            if (appointment.getServiceId() != null) {
                Optional<Service> serviceOptional = serviceRepository.findById(appointment.getServiceId());
                if (!serviceOptional.isPresent()) {
                    return new ResponseEntity<>("Service with ID '" + appointment.getServiceId() + "' not found", HttpStatus.NOT_FOUND);
                }
                appointment.setService(serviceOptional.get());
            } else if (appointment.getService() == null || appointment.getService().getId() == null) {
                return new ResponseEntity<>("Service ID is required", HttpStatus.BAD_REQUEST);
            } else {
                Optional<Service> serviceOptional = serviceRepository.findById(appointment.getService().getId());
                if (!serviceOptional.isPresent()) {
                    return new ResponseEntity<>("Service with ID '" + appointment.getService().getId() + "' not found", HttpStatus.NOT_FOUND);
                }
                appointment.setService(serviceOptional.get());
            }
            
            // Set default status if not provided
            if (appointment.getStatus() == null || appointment.getStatus().trim().isEmpty()) {
                appointment.setStatus("PENDING");
            }
            
            // Check for double booking (optional)
            if (appointment.getSpecialist() != null) {
                boolean exists = appointmentRepository.existsBySpecialistIdAndAppointmentTime(
                    appointment.getSpecialist().getId(),
                    appointment.getAppointmentTime()
                );
                if (exists) {
                    return new ResponseEntity<>("Specialist is already booked for this time", HttpStatus.CONFLICT);
                }
            }
            
            Appointment saved = appointmentRepository.save(appointment);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error creating appointment: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Appointment>> getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentTime") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("DESC") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Appointment> appointments = appointmentRepository.findAll(pageable);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isPresent()) {
            return new ResponseEntity<>(appointment.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Appointment not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Appointment>> getAppointmentsByUser(@PathVariable Long userId) {
        List<Appointment> appointments = appointmentRepository.findByUserIdWithRelationships(userId);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping(value = "/specialist/{specialistId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Appointment>> getAppointmentsBySpecialist(@PathVariable Long specialistId) {
        List<Appointment> appointments = appointmentRepository.findBySpecialistIdWithRelationships(specialistId);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping(value = "/status/{status}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Appointment>> getAppointmentsByStatus(@PathVariable String status) {
        // You'll need to add this method to repository
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointment) {
        try {
            Optional<Appointment> existingOptional = appointmentRepository.findById(id);
            if (!existingOptional.isPresent()) {
                return new ResponseEntity<>("Appointment not found", HttpStatus.NOT_FOUND);
            }
            
            Appointment existing = existingOptional.get();
            
            // Update fields
            if (appointment.getAppointmentTime() != null) {
                existing.setAppointmentTime(appointment.getAppointmentTime());
            }
            if (appointment.getNotes() != null) {
                existing.setNotes(appointment.getNotes());
            }
            if (appointment.getStatus() != null) {
                existing.setStatus(appointment.getStatus());
            }
            
            Appointment updated = appointmentRepository.save(existing);
            return new ResponseEntity<>(updated, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error updating appointment: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        try {
            Optional<Appointment> appointment = appointmentRepository.findById(id);
            if (!appointment.isPresent()) {
                return new ResponseEntity<>("Appointment not found", HttpStatus.NOT_FOUND);
            }
            
            appointmentRepository.deleteById(id);
            return new ResponseEntity<>("Appointment deleted successfully", HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error deleting appointment: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
