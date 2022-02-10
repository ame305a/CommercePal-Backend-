package com.commerce.pal.backend.repo.app_settings;

import com.commerce.pal.backend.models.app_settings.TargetSchema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TargetSchemaRepository extends JpaRepository<TargetSchema, Integer> {

    Optional<TargetSchema> findTargetSchemaByTargetKeyAndSchemaSettingId(String key, Integer id);

    Optional<TargetSchema> findTargetSchemaById(Integer key);


    List<TargetSchema> findTargetSchemaBySchemaSettingId(Integer id);
}
