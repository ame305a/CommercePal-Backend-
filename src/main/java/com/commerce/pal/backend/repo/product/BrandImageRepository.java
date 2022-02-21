package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.BrandImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandImageRepository extends JpaRepository<BrandImage, Long> {

    Optional<BrandImage> findBrandImageByBrand(String brand);
}
