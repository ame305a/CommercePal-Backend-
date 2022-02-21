package com.commerce.pal.backend.repo.app_settings;

import com.commerce.pal.backend.models.app_settings.TargetSchema;
import com.commerce.pal.backend.models.app_settings.TargetSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TargetSectionRepository extends JpaRepository<TargetSection, Integer> {

    Optional<TargetSection> findTargetSectionBySectionKeyAndTargetId(String key, Integer id);

    Optional<TargetSection> findTargetSectionById(Integer key);


    List<TargetSection> findTargetSectionByTargetId(Integer target);

}
