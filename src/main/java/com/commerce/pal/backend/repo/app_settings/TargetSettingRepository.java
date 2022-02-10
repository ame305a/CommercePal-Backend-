package com.commerce.pal.backend.repo.app_settings;

import com.commerce.pal.backend.models.app_settings.TargetSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TargetSettingRepository extends JpaRepository<TargetSetting, Integer> {

    Optional<TargetSetting> findTargetSettingByTarget(String target);


}
