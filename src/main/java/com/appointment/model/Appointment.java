package com.appointment.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"appointments", "userProfile", "password"})
    private User user;
    
    // Allow setting user by ID
    @Transient
    @JsonProperty("userId")
    private Long userId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "specialist_id")
    @JsonIgnoreProperties({"appointments", "userProfile", "password"})
    private User specialist;
    
    // Allow setting specialist by ID
    @Transient
    @JsonProperty("specialistId")
    private Long specialistId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id")
    @JsonIgnoreProperties({"categories"})
    private Service service;
    
    // Allow setting service by ID
    @Transient
    @JsonProperty("serviceId")
    private Long serviceId;

    @Column(nullable = false)
    private LocalDateTime appointmentTime;

    private String notes;

    private String status;
}
