package com.commerce.pal.backend.repo.product.categories;

import com.commerce.pal.backend.models.product.categories.BrandImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandImageRepository extends JpaRepository<BrandImage, Long> {

    Optional<BrandImage> findBrandImageByBrand(String brand);


    List<BrandImage> findBrandImagesByParentCategoryId(Long parentCat);
}
