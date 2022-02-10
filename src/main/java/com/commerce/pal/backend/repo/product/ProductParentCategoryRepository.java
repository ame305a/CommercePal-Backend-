package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.ProductParentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductParentCategoryRepository extends JpaRepository<ProductParentCategory, Long> {
}
