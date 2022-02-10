package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.ProductSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductSubCategoryRepository extends JpaRepository<ProductSubCategory, Long> {

    List<ProductSubCategory> findProductSubCategoriesByProductCategoryId(Long category);
}
