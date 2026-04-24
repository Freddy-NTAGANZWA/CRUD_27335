package com.appointment.repository;

import com.appointment.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.village.parent.parent.parent.parent.code = :provinceCode")
    List<User> findByProvinceCode(@Param("provinceCode") String provinceCode);
    
    @Query("SELECT u FROM User u WHERE u.village.parent.parent.parent.parent.name = :provinceName")
    List<User> findByProvinceName(@Param("provinceName") String provinceName);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.village LEFT JOIN FETCH u.role WHERE u.id = :id")
    Optional<User> findUserWithLocationAndRole(@Param("id") Long id);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Page<User> findAll(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
}
