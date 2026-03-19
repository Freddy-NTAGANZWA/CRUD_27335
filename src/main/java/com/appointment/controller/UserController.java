package com.appointment.controller;

import com.appointment.model.User;
import com.appointment.model.Role;
import com.appointment.model.Location;
import com.appointment.repository.UserRepository;
import com.appointment.repository.RoleRepository;
import com.appointment.repository.LocationRepository;
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
@RequestMapping("/api/users")
public class UserController {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LocationRepository locationRepository;

    public UserController(UserRepository userRepository,
                         RoleRepository roleRepository,
                         LocationRepository locationRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.locationRepository = locationRepository;
    }

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            // Validate required fields
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                return new ResponseEntity<>("User name is required", HttpStatus.BAD_REQUEST);
            }
            
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
            }
            
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return new ResponseEntity<>("Password is required", HttpStatus.BAD_REQUEST);
            }
            
            // Check if email already exists
            if (userRepository.existsByEmail(user.getEmail())) {
                return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
            }
            
            // Handle role
            if (user.getRole() != null && user.getRole().getId() != null) {
                Optional<Role> roleOptional = roleRepository.findById(user.getRole().getId());
                if (!roleOptional.isPresent()) {
                    return new ResponseEntity<>("Role with ID '" + user.getRole().getId() + "' not found", HttpStatus.NOT_FOUND);
                }
                user.setRole(roleOptional.get());
            }
            
            // Handle village (location)
            if (user.getVillage() != null && user.getVillage().getId() != null) {
                Optional<Location> villageOptional = locationRepository.findById(user.getVillage().getId());
                if (!villageOptional.isPresent()) {
                    return new ResponseEntity<>("Village with ID '" + user.getVillage().getId() + "' not found", HttpStatus.NOT_FOUND);
                }
                user.setVillage(villageOptional.get());
            }
            
            User saved = userRepository.save(user);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error creating user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("DESC") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = userRepository.findAll(pageable);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/email/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            return new ResponseEntity<>(user.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/exists/email/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        return new ResponseEntity<>(userRepository.existsByEmail(email), HttpStatus.OK);
    }

    @GetMapping(value = "/province/code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> getUsersByProvinceCode(@PathVariable String code) {
        List<User> users = userRepository.findByProvinceCode(code);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping(value = "/province/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> getUsersByProvinceName(@PathVariable String name) {
        List<User> users = userRepository.findByProvinceName(name);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            Optional<User> existingOptional = userRepository.findById(id);
            if (!existingOptional.isPresent()) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
            
            User existing = existingOptional.get();
            
            // Update fields
            if (user.getName() != null && !user.getName().trim().isEmpty()) {
                existing.setName(user.getName());
            }
            
            if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                // Check if new email already exists (excluding current user)
                Optional<User> emailUser = userRepository.findByEmail(user.getEmail());
                if (emailUser.isPresent() && !emailUser.get().getId().equals(id)) {
                    return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
                }
                existing.setEmail(user.getEmail());
            }
            
            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                existing.setPassword(user.getPassword());
            }
            
            if (user.getRole() != null && user.getRole().getId() != null) {
                Optional<Role> roleOptional = roleRepository.findById(user.getRole().getId());
                if (roleOptional.isPresent()) {
                    existing.setRole(roleOptional.get());
                }
            }
            
            if (user.getVillage() != null && user.getVillage().getId() != null) {
                Optional<Location> villageOptional = locationRepository.findById(user.getVillage().getId());
                if (villageOptional.isPresent()) {
                    existing.setVillage(villageOptional.get());
                }
            }
            
            User updated = userRepository.save(existing);
            return new ResponseEntity<>(updated, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error updating user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (!user.isPresent()) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
            
            userRepository.deleteById(id);
            return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error deleting user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
