package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.ProductFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProductFeatureRepository extends JpaRepository<ProductFeature, Long> {

    List<ProductFeature> findProductFeaturesBySubCategoryId(Long sub);


    Optional<ProductFeature> findByIdAndSubCategoryId(Long id, Long sub);


    @Transactional
    Long removeProductFeatureById(Long id);
}
