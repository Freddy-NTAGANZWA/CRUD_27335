package com.appointment.service;

import com.appointment.model.Appointment;
import com.appointment.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    public Appointment createAppointment(Appointment appointment) {
        // Validation: Prevent double-booking for the same specialist at the same time
        if (appointment.getSpecialist() != null && appointment.getAppointmentTime() != null) {
            boolean exists = appointmentRepository.existsBySpecialistIdAndAppointmentTime(
                    appointment.getSpecialist().getId(),
                    appointment.getAppointmentTime());
            if (exists) {
                throw new RuntimeException("Double-Booking: Specialist is already booked for this date and time.");
            }
        }

        Appointment createdAppointment = appointmentRepository.save(appointment);
        return appointmentRepository.findByIdWithRelationships(createdAppointment.getId())
                .orElseThrow(() -> new RuntimeException("Appointment not found after creation"));
    }

    public List<Appointment> getAppointmentsByRole(String role, Long userId) {
        if ("ADMIN".equals(role)) {
            return appointmentRepository.findAllWithRelationships();
        } else if ("STAFF".equals(role)) {
            return appointmentRepository.findBySpecialistIdWithRelationships(userId);
        } else {
            // Default: CUSTOMER
            return appointmentRepository.findByUserIdWithRelationships(userId);
        }
    }

    public Appointment getAppointmentWithRelationships(Long id) {
        return appointmentRepository.findByIdWithRelationships(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
    }
}