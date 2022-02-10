package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findProductCategoriesByParentCategoryId(Long id);
}
