package com.appointment.repository;

import com.appointment.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    @Query("SELECT up FROM UserProfile up LEFT JOIN FETCH up.user WHERE up.user.id = :userId")
    Optional<UserProfile> findByUserIdWithUser(@Param("userId") Long userId);
}
