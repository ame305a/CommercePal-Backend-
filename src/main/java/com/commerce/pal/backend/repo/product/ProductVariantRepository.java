package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long product);
    Optional<ProductVariant> findByProductIdAndProductVariantId(Long aLong, Long sub);

}
