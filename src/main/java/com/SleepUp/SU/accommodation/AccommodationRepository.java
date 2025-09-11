package com.SleepUp.SU.accommodation;

import com.SleepUp.SU.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccommodationRepository extends JpaRepository <Accommodation, Long>{
    List<Accommodation> findByManagedBy_Id(Long userId);

}
