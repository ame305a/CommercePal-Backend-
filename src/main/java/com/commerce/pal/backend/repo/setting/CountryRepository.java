package com.commerce.pal.backend.repo.setting;

import com.commerce.pal.backend.models.setting.Country;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<Country, Long> {
}
