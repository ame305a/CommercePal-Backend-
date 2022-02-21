package com.commerce.pal.backend.repo.app_settings;

import com.commerce.pal.backend.models.app_settings.TargetSectionChildren;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TargetSectionChildrenRepository extends JpaRepository<TargetSectionChildren, Integer> {

    List<TargetSectionChildren> findTargetSectionChildrenByTargetSectionId(Integer section);

    Optional<TargetSectionChildren> findTargetSectionChildrenByTargetSectionIdAndItemId(Integer section,Long itemId);
}
