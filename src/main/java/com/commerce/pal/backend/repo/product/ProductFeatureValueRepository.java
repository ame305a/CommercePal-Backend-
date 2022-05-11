package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.ProductFeatureValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductFeatureValueRepository extends JpaRepository<ProductFeatureValue, Integer> {
}
