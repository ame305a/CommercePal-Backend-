package com.commerce.pal.backend.repo.setting;

import com.commerce.pal.backend.models.setting.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByCityIgnoreCase(String cityName);
}
