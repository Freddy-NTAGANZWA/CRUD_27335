package com.appointment.config;

import com.appointment.model.*;
import com.appointment.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedData(
            LocationRepository locationRepository,
            RoleRepository roleRepository,
            UserRepository userRepository) {
        return args -> {

            // ── Roles (always ensure all 3 exist) ─────────────────────────────
            if (!roleRepository.existsByName("CUSTOMER"))
                roleRepository.save(Role.builder().name("CUSTOMER").build());
            if (!roleRepository.existsByName("STAFF"))
                roleRepository.save(Role.builder().name("STAFF").build());
            if (!roleRepository.existsByName("ADMIN"))
                roleRepository.save(Role.builder().name("ADMIN").build());

            // ── Default accounts (always ensure they exist) ────────────────────
            Role adminRole    = roleRepository.findByName("ADMIN").orElseThrow();
            Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();

            if (!userRepository.existsByEmail("admin@servicebook.rw")) {
                userRepository.save(User.builder()
                        .name("System Admin")
                        .email("admin@servicebook.rw")
                        .password("admin123")
                        .role(adminRole)
                        .build());
                System.out.println(">>> Admin account created: admin@servicebook.rw / admin123");
            }

            if (!userRepository.existsByEmail("user@servicebook.rw")) {
                userRepository.save(User.builder()
                        .name("Demo User")
                        .email("user@servicebook.rw")
                        .password("user123")
                        .role(customerRole)
                        .build());
                System.out.println(">>> Demo user created: user@servicebook.rw / user123");
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
