package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.ProductFeature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductFeatureRepository extends JpaRepository<ProductFeature, Long> {
}
