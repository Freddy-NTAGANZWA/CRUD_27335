package com.appointment.config;

import com.appointment.model.*;
import com.appointment.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedData(
            LocationRepository locationRepository,
            RoleRepository roleRepository,
            UserRepository userRepository) {
        return args -> {

            // ── Roles ──────────────────────────────────────────────────────────
            if (roleRepository.count() == 0) {
                Role customer = roleRepository.save(Role.builder().name("CUSTOMER").build());
                Role staff    = roleRepository.save(Role.builder().name("STAFF").build());
                Role admin    = roleRepository.save(Role.builder().name("ADMIN").build());

                // ── Default admin account ──────────────────────────────────────
                if (!userRepository.existsByEmail("admin@servicebook.rw")) {
                    userRepository.save(User.builder()
                            .name("System Admin")
                            .email("admin@servicebook.rw")
                            .password("admin123")
                            .role(admin)
                            .build());
                }

                // ── Demo customer account ──────────────────────────────────────
                if (!userRepository.existsByEmail("user@servicebook.rw")) {
                    userRepository.save(User.builder()
                            .name("Demo User")
                            .email("user@servicebook.rw")
                            .password("user123")
                            .role(customer)
                            .build());
                }
            }

            // ── Locations ──────────────────────────────────────────────────────
            if (locationRepository.count() == 0) {
                Location kigali = locationRepository.save(Location.builder()
                        .name("Kigali City").type(LocationType.PROVINCE).code("KGL").build());

                Location gasabo = locationRepository.save(Location.builder()
                        .name("Gasabo").type(LocationType.DISTRICT).code("KGL-GSB").parent(kigali).build());

                Location remera = locationRepository.save(Location.builder()
                        .name("Remera").type(LocationType.SECTOR).code("KGL-GSB-RMR").parent(gasabo).build());

                Location rukiri = locationRepository.save(Location.builder()
                        .name("Rukiri").type(LocationType.CELL).code("KGL-GSB-RMR-RKR").parent(remera).build());

                locationRepository.save(Location.builder()
                        .name("Rukiri I").type(LocationType.VILLAGE).code("KGL-GSB-RMR-RKR-V1").parent(rukiri).build());
            }
        };
    }
}
