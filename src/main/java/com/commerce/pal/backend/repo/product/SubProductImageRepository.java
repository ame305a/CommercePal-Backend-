package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.SubProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubProductImageRepository extends JpaRepository<SubProductImage, Long> {
    List<SubProductImage> findSubProductImagesBySubProductId(Long sub);
}
