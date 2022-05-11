package com.commerce.pal.backend.repo.product.categories;

import com.commerce.pal.backend.models.product.categories.ProductParentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductParentCategoryRepository extends JpaRepository<ProductParentCategory, Long> {
}
