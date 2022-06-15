package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findProductImagesByProductIdAndStatus(Long product, Integer status);

    Long deleteProductImageByProductIdAndFilePath(Long product, String imageName);

    @Transactional
    Long removeProductImageByProductIdAndFilePath(Long product, String imageName);

    Optional<ProductImage> findProductImageById(Long id);
}
