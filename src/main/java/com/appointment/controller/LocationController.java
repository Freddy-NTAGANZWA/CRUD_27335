package com.appointment.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.appointment.model.Location;
import com.appointment.repository.LocationRepository;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationRepository locationRepository;

    public LocationController(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    // ── CREATE Province (no parent) ───────────────────────────────────────────
    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveLocation(@RequestBody Location location) {
        if (location.getCode() != null && locationRepository.existsByCode(location.getCode()))
            return new ResponseEntity<>("Location with code '" + location.getCode() + "' already exists", HttpStatus.CONFLICT);
        location.setParent(null);
        return new ResponseEntity<>(locationRepository.save(location), HttpStatus.CREATED);
    }

    // ── CREATE child location (with parent) ───────────────────────────────────
    @PostMapping(value = "/saveWithParent", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveLocationWithParent(
            @RequestParam(required = false) String parentCode,
            @RequestParam(required = false) Long parentId,
            @RequestBody Location location) {
        try {
            Location parent = null;

            if (parentId != null) {
                Optional<Location> p = locationRepository.findById(parentId);
                if (p.isEmpty()) return new ResponseEntity<>("Parent ID '" + parentId + "' not found", HttpStatus.NOT_FOUND);
                parent = p.get();
            } else if (location.getParentId() != null) {
                Optional<Location> p = locationRepository.findById(location.getParentId());
                if (p.isEmpty()) return new ResponseEntity<>("Parent ID '" + location.getParentId() + "' not found", HttpStatus.NOT_FOUND);
                parent = p.get();
            } else if (parentCode != null && !parentCode.isBlank()) {
                Optional<Location> p = locationRepository.findByCode(parentCode);
                if (p.isEmpty()) return new ResponseEntity<>("Parent code '" + parentCode + "' not found", HttpStatus.NOT_FOUND);
                parent = p.get();
            } else if (location.getParent() != null && location.getParent().getId() != null) {
                Optional<Location> p = locationRepository.findById(location.getParent().getId());
                if (p.isEmpty()) return new ResponseEntity<>("Parent not found", HttpStatus.NOT_FOUND);
                parent = p.get();
            } else {
                return new ResponseEntity<>("Parent is required for child locations", HttpStatus.BAD_REQUEST);
            }

            if (location.getName() == null || location.getName().isBlank())
                return new ResponseEntity<>("Location name is required", HttpStatus.BAD_REQUEST);
            if (location.getType() == null)
                return new ResponseEntity<>("Location type is required", HttpStatus.BAD_REQUEST);
            if (location.getCode() != null && locationRepository.existsByCode(location.getCode()))
                return new ResponseEntity<>("Location with code '" + location.getCode() + "' already exists", HttpStatus.CONFLICT);

            location.setParent(parent);
            return new ResponseEntity<>(locationRepository.save(location), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error saving location: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── READ all ──────────────────────────────────────────────────────────────
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Location>> getAllLocations() {
        return new ResponseEntity<>(locationRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getLocationById(@PathVariable Long id) {
        return locationRepository.findById(id)
            .<ResponseEntity<?>>map(l -> new ResponseEntity<>(l, HttpStatus.OK))
            .orElse(new ResponseEntity<>("Location not found", HttpStatus.NOT_FOUND));
    }

    @GetMapping(value = "/provinces", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Location>> getProvinces() {
        return new ResponseEntity<>(locationRepository.findByType(com.appointment.model.LocationType.PROVINCE), HttpStatus.OK);
    }

    @GetMapping(value = "/byParent/{parentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Location>> getLocationsByParent(@PathVariable Long parentId) {
        return new ResponseEntity<>(locationRepository.findByParentId(parentId), HttpStatus.OK);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateLocation(@PathVariable Long id, @RequestBody Location location) {
        try {
            Optional<Location> existingOpt = locationRepository.findById(id);
            if (existingOpt.isEmpty())
                return new ResponseEntity<>("Location not found", HttpStatus.NOT_FOUND);

            Location existing = existingOpt.get();
            if (location.getName() != null && !location.getName().isBlank())
                existing.setName(location.getName());
            if (location.getCode() != null && !location.getCode().isBlank()) {
                if (!location.getCode().equals(existing.getCode()) && locationRepository.existsByCode(location.getCode()))
                    return new ResponseEntity<>("Code already exists", HttpStatus.CONFLICT);
                existing.setCode(location.getCode());
            }
            if (location.getType() != null) existing.setType(location.getType());

            return new ResponseEntity<>(locationRepository.save(existing), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating location: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteLocation(@PathVariable Long id) {
        try {
            if (!locationRepository.existsById(id))
                return new ResponseEntity<>("Location not found", HttpStatus.NOT_FOUND);
            // Check if it has children
            List<Location> children = locationRepository.findByParentId(id);
            if (!children.isEmpty())
                return new ResponseEntity<>(
                    "Cannot delete: this location has " + children.size() + " sub-location(s). Delete them first.",
                    HttpStatus.CONFLICT);
            locationRepository.deleteById(id);
            return new ResponseEntity<>("Location deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting location: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
