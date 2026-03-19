package com.appointment.controller;

import com.appointment.model.UserProfile;
import com.appointment.model.User;
import com.appointment.repository.UserProfileRepository;
import com.appointment.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/profiles")
public class UserProfileController {
    
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public UserProfileController(UserProfileRepository userProfileRepository,
                                UserRepository userRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createUserProfile(@RequestBody UserProfile userProfile) {
        try {
            // Validate user
            if (userProfile.getUser() == null || userProfile.getUser().getId() == null) {
                return new ResponseEntity<>("User ID is required", HttpStatus.BAD_REQUEST);
            }
            
            // Check if user exists
            Optional<User> userOptional = userRepository.findById(userProfile.getUser().getId());
            if (!userOptional.isPresent()) {
                return new ResponseEntity<>("User with ID '" + userProfile.getUser().getId() + "' not found", HttpStatus.NOT_FOUND);
            }
            
            // Check if user already has a profile
            Optional<UserProfile> existingProfile = userProfileRepository.findByUserIdWithUser(userProfile.getUser().getId());
            if (existingProfile.isPresent()) {
                return new ResponseEntity<>("User already has a profile", HttpStatus.CONFLICT);
            }
            
            userProfile.setUser(userOptional.get());
            UserProfile saved = userProfileRepository.save(userProfile);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error creating user profile: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserProfile>> getAllUserProfiles() {
        List<UserProfile> profiles = userProfileRepository.findAll();
        return new ResponseEntity<>(profiles, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserProfileById(@PathVariable Long id) {
        Optional<UserProfile> profile = userProfileRepository.findById(id);
        if (profile.isPresent()) {
            return new ResponseEntity<>(profile.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User profile not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserProfileByUserId(@PathVariable Long userId) {
        Optional<UserProfile> profile = userProfileRepository.findByUserIdWithUser(userId);
        if (profile.isPresent()) {
            return new ResponseEntity<>(profile.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User profile not found", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUserProfile(@PathVariable Long id, @RequestBody UserProfile userProfile) {
        try {
            Optional<UserProfile> existingOptional = userProfileRepository.findById(id);
            if (!existingOptional.isPresent()) {
                return new ResponseEntity<>("User profile not found", HttpStatus.NOT_FOUND);
            }
            
            UserProfile existing = existingOptional.get();
            
            // Update fields
            if (userProfile.getPhone() != null) {
                existing.setPhone(userProfile.getPhone());
            }
            if (userProfile.getDateOfBirth() != null) {
                existing.setDateOfBirth(userProfile.getDateOfBirth());
            }
            if (userProfile.getGender() != null) {
                existing.setGender(userProfile.getGender());
            }
            
            UserProfile updated = userProfileRepository.save(existing);
            return new ResponseEntity<>(updated, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error updating user profile: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteUserProfile(@PathVariable Long id) {
        try {
            Optional<UserProfile> profile = userProfileRepository.findById(id);
            if (!profile.isPresent()) {
                return new ResponseEntity<>("User profile not found", HttpStatus.NOT_FOUND);
            }
            
            userProfileRepository.deleteById(id);
            return new ResponseEntity<>("User profile deleted successfully", HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error deleting user profile: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
