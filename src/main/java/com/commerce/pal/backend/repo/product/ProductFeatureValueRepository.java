package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.ProductFeatureValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProductFeatureValueRepository extends JpaRepository<ProductFeatureValue, Long> {

    List<ProductFeatureValue> findAllByProductId(Long subProduct);

    @Query(value = "SELECT ProductFeatureId  FROM ProductFeatureValue WHERE ProductId IN ?1 GROUP BY ProductFeatureId", nativeQuery = true)
    List<Long> findProductFeatureValuesByProductId(List<Long> subs);

    @Query(value = " SELECT Value  FROM ProductFeatureValue WHERE ProductFeatureId = ?1 GROUP BY Value", nativeQuery = true)
    List<String> findProductFeatureValuesByProductFeatureId(Long featureId);


    Optional<ProductFeatureValue> findProductFeatureValuesByProductFeatureIdAndProductId(Long featureId, Long subProdId);

    @Transactional
    Long removeProductFeatureValueById(Long id);
}
