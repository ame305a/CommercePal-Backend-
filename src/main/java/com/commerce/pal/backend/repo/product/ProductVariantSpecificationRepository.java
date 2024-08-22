package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.ProductVariantSpecification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ProductVariantSpecificationRepository extends JpaRepository<ProductVariantSpecification, Long> {

    Optional<ProductVariantSpecification> findByProductFeatureIdAndProductVariantId(long featureId, Long productVariantId);

    List<ProductVariantSpecification> findByProductIdAndProductVariantId(long productId, Long productVariantId);
}
