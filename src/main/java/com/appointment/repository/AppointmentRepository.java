package com.appointment.repository;

import com.appointment.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Page<Appointment> findAll(Pageable pageable);
    
    boolean existsBySpecialistIdAndAppointmentTime(Long specialistId, LocalDateTime appointmentTime);
    
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.user LEFT JOIN FETCH a.specialist LEFT JOIN FETCH a.service WHERE a.id = :id")
    Optional<Appointment> findByIdWithRelationships(@Param("id") Long id);
    
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.user LEFT JOIN FETCH a.specialist LEFT JOIN FETCH a.service")
    List<Appointment> findAllWithRelationships();
    
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.user LEFT JOIN FETCH a.specialist LEFT JOIN FETCH a.service WHERE a.specialist.id = :specialistId")
    List<Appointment> findBySpecialistIdWithRelationships(@Param("specialistId") Long specialistId);
    
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.user LEFT JOIN FETCH a.specialist LEFT JOIN FETCH a.service WHERE a.user.id = :userId")
    List<Appointment> findByUserIdWithRelationships(@Param("userId") Long userId);
}
