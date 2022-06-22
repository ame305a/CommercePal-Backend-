package com.commerce.pal.backend.repo.product.categories;

import com.commerce.pal.backend.models.product.categories.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findProductCategoriesByParentCategoryId(Long id);

    Optional<ProductCategory> findProductCategoryByIdAndParentCategoryId(Long id, Long parent);
}
