package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.promotion.ProductPromoCode;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductRepositoryV2 extends JpaRepository<ProductPromoCode, Long> {

}
