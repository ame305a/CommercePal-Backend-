package com.commerce.pal.backend.repo.setting;

import com.commerce.pal.backend.models.setting.AppVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppVersionRepository extends JpaRepository<AppVersion, Integer> {
}
