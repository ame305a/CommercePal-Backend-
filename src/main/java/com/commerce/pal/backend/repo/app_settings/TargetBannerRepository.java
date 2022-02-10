package com.commerce.pal.backend.repo.app_settings;

import com.commerce.pal.backend.models.app_settings.TargetBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TargetBannerRepository extends JpaRepository<TargetBanner, Integer> {
    Boolean existsAllBySchemaSettingId(Integer id);

    List<TargetBanner> findTargetBannersBySchemaSettingId(Integer id);

    List<TargetBanner> findTargetBannersBySchemaSettingIdAndType(Integer id, String type);
}
