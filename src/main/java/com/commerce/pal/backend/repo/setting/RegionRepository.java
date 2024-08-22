package com.commerce.pal.backend.repo.setting;

import com.commerce.pal.backend.models.setting.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Integer> {
    Optional<Region> findByRegionNameIgnoreCase(String regionName);
}
