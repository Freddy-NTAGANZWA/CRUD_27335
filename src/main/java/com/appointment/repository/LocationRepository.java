package com.appointment.repository;

import com.appointment.model.Location;
import com.appointment.model.LocationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByCode(String code);
    List<Location> findByType(LocationType type);
    List<Location> findByParentId(Long parentId);
    boolean existsByCode(String code);
    boolean existsByName(String name);
}
