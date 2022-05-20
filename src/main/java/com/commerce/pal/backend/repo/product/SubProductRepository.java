package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.SubProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubProductRepository extends JpaRepository<SubProduct, Long> {
    List<SubProduct> findSubProductsByProductId(Long product);


    Optional<SubProduct> findSubProductsByProductIdAndSubProductId(Long aLong, Long sub);
}
