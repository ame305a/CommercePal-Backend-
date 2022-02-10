package com.commerce.pal.backend.repo.app_settings;

import com.commerce.pal.backend.models.app_settings.SchemaCatalogue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchemaCatalogueRepository extends JpaRepository<SchemaCatalogue, Integer> {

    List<SchemaCatalogue> findSchemaCataloguesByTargetSchemaId(Integer target);
}
