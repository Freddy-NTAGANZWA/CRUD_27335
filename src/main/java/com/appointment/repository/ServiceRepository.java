package com.appointment.repository;

import com.appointment.model.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    Page<Service> findAll(Pageable pageable);
    boolean existsByName(String name);
    List<Service> findByCategoriesId(Long categoryId);
}
