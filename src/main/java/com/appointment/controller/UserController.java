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
import java.util.Map;
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

    // ── AUTH ──────────────────────────────────────────────────────────────────

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email    = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || password == null)
            return new ResponseEntity<>("Email and password are required", HttpStatus.BAD_REQUEST);

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty())
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);

        User user = userOpt.get();
        if (!user.getPassword().equals(password))
            return new ResponseEntity<>("Invalid email or password", HttpStatus.UNAUTHORIZED);

        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // ── REGISTER (self-service, always CUSTOMER role) ─────────────────────────

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> register(@RequestBody User user) {
        if (user.getName() == null || user.getName().isBlank())
            return new ResponseEntity<>("Name is required", HttpStatus.BAD_REQUEST);
        if (user.getEmail() == null || user.getEmail().isBlank())
            return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
        if (user.getPassword() == null || user.getPassword().isBlank())
            return new ResponseEntity<>("Password is required", HttpStatus.BAD_REQUEST);
        if (userRepository.existsByEmail(user.getEmail()))
            return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);

        // Force CUSTOMER role on self-registration
        Optional<Role> customerRole = roleRepository.findByName("CUSTOMER");
        customerRole.ifPresent(user::setRole);

        User saved = userRepository.save(user);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            if (user.getName() == null || user.getName().isBlank())
                return new ResponseEntity<>("User name is required", HttpStatus.BAD_REQUEST);
            if (user.getEmail() == null || user.getEmail().isBlank())
                return new ResponseEntity<>("Email is required", HttpStatus.BAD_REQUEST);
            if (user.getPassword() == null || user.getPassword().isBlank())
                return new ResponseEntity<>("Password is required", HttpStatus.BAD_REQUEST);
            if (userRepository.existsByEmail(user.getEmail()))
                return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);

            if (user.getRole() != null && user.getRole().getId() != null) {
                Optional<Role> roleOpt = roleRepository.findById(user.getRole().getId());
                if (roleOpt.isEmpty())
                    return new ResponseEntity<>("Role not found", HttpStatus.NOT_FOUND);
                user.setRole(roleOpt.get());
            }

            if (user.getVillage() != null && user.getVillage().getId() != null) {
                Optional<Location> villageOpt = locationRepository.findById(user.getVillage().getId());
                if (villageOpt.isEmpty())
                    return new ResponseEntity<>("Village not found", HttpStatus.NOT_FOUND);
                user.setVillage(villageOpt.get());
            }

            return new ResponseEntity<>(userRepository.save(user), HttpStatus.CREATED);
        } catch (Exception e) {
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
            ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return new ResponseEntity<>(userRepository.findAll(pageable), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
            .<ResponseEntity<?>>map(u -> new ResponseEntity<>(u, HttpStatus.OK))
            .orElse(new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND));
    }

    @GetMapping(value = "/email/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return userRepository.findByEmail(email)
            .<ResponseEntity<?>>map(u -> new ResponseEntity<>(u, HttpStatus.OK))
            .orElse(new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND));
    }

    @GetMapping(value = "/exists/email/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        return new ResponseEntity<>(userRepository.existsByEmail(email), HttpStatus.OK);
    }

    @GetMapping(value = "/province/code/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> getUsersByProvinceCode(@PathVariable String code) {
        return new ResponseEntity<>(userRepository.findByProvinceCode(code), HttpStatus.OK);
    }

    @GetMapping(value = "/province/name/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<User>> getUsersByProvinceName(@PathVariable String name) {
        return new ResponseEntity<>(userRepository.findByProvinceName(name), HttpStatus.OK);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            Optional<User> existingOpt = userRepository.findById(id);
            if (existingOpt.isEmpty())
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);

            User existing = existingOpt.get();

            if (user.getName() != null && !user.getName().isBlank()) existing.setName(user.getName());

            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                Optional<User> emailUser = userRepository.findByEmail(user.getEmail());
                if (emailUser.isPresent() && !emailUser.get().getId().equals(id))
                    return new ResponseEntity<>("Email already exists", HttpStatus.CONFLICT);
                existing.setEmail(user.getEmail());
            }

            if (user.getPassword() != null && !user.getPassword().isBlank()) existing.setPassword(user.getPassword());

            if (user.getRole() != null && user.getRole().getId() != null)
                roleRepository.findById(user.getRole().getId()).ifPresent(existing::setRole);

            if (user.getVillage() != null && user.getVillage().getId() != null)
                locationRepository.findById(user.getVillage().getId()).ifPresent(existing::setVillage);

            return new ResponseEntity<>(userRepository.save(existing), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            if (!userRepository.existsById(id))
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            userRepository.deleteById(id);
            return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
