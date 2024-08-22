package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    List<ProductReview> findByProductIdAndStatus(Long productId, int status);

    List<ProductReview> findByVerifiedTrueOrderByCreatedAtDesc();

    List<ProductReview> findByProductIdAndVerifiedTrue(Long productId);

    List<ProductReview> findByProductId(Long productId);

    List<ProductReview> findByProductIdAndStatusOrderByHelpfulCountDescRatingDescCreatedAtDescVerifiedDesc(Long productId, int i);
}
