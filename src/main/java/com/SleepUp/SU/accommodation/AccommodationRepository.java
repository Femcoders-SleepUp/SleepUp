package com.SleepUp.SU.accommodation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccommodationRepository extends JpaRepository <Accommodation, Long>, JpaSpecificationExecutor<Accommodation> {
    List<Accommodation> findByManagedBy_Id(Long userId);
    boolean existsByName(String name);
}
