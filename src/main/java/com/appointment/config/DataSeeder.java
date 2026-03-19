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
            if (roleRepository.count() == 0) {
                Role customer = Role.builder().name("CUSTOMER").build();
                Role staff = Role.builder().name("STAFF").build();
                Role admin = Role.builder().name("ADMIN").build();
                roleRepository.saveAll(Arrays.asList(customer, staff, admin));
            }

            if (locationRepository.count() == 0) {
                // Province Level (Level 1)
                Location kigali = Location.builder()
                        .name("Kigali City")
                        .type(LocationType.PROVINCE)
                        .code("KGL")
                        .build();
                locationRepository.save(kigali);

                // District Level (Level 2)
                Location gasabo = Location.builder()
                        .name("Gasabo")
                        .type(LocationType.DISTRICT)
                        .code("KGL-GSB")
                        .parent(kigali)
                        .build();
                locationRepository.save(gasabo);

                // Sector Level (Level 3)
                Location remera = Location.builder()
                        .name("Remera")
                        .type(LocationType.SECTOR)
                        .code("KGL-GSB-RMR")
                        .parent(gasabo)
                        .build();
                locationRepository.save(remera);

                // Cell Level (Level 4)
                Location rukiri = Location.builder()
                        .name("Rukiri")
                        .type(LocationType.CELL)
                        .code("KGL-GSB-RMR-RKR")
                        .parent(remera)
                        .build();
                locationRepository.save(rukiri);

                // Village Level (Level 5)
                Location village1 = Location.builder()
                        .name("Rukiri I")
                        .type(LocationType.VILLAGE)
                        .code("KGL-GSB-RMR-RKR-V1")
                        .parent(rukiri)
                        .build();
                locationRepository.save(village1);
            }
        };
    }
}
