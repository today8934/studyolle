package com.studyolle.zone;

import com.studyolle.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Optional<Zone> findByCityAndLocalNameOfCity(String city, String localNameOfCity);
}
