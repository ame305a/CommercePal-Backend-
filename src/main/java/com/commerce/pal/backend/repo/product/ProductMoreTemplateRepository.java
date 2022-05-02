package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.ProductMoreTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductMoreTemplateRepository extends JpaRepository<ProductMoreTemplate, Integer> {
    List<ProductMoreTemplate> findProductMoreTemplateByProductMoreId(Integer more);
}
