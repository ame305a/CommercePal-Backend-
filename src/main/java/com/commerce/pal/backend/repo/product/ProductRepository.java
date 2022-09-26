package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findProductByProductId(Long product);

    Optional<Product> findProductByOwnerTypeAndMerchantIdAndProductId(String type, Long merchant, Long product);

    List<Product> findProductsByProductParentCateoryIdAndProductCategoryIdAndProductSubCategoryId(Long par, Long cat, Long sub);

    @Query(value = "SELECT ProductParentCateoryId  FROM Product WHERE MerchantId = ?1  GROUP BY ProductParentCateoryId", nativeQuery = true)
    List<Long> findProductsByProductParentCateoryId(Long merchant);

    @Query(value = "SELECT ProductCategoryId  FROM Product WHERE MerchantId = ?1 AND ProductParentCateoryId= ?2 GROUP BY ProductCategoryId", nativeQuery = true)
    List<Long> findProductsByProductCategoryId(Long merchant, Long parent);

    @Query(value = "SELECT ProductSubCategoryId  FROM Product WHERE MerchantId = ?1 AND ProductCategoryId = ?2  GROUP BY ProductSubCategoryId", nativeQuery = true)
    List<Long> findProductsByProductSubCategoryId(Long merchant, Long category);

    List<Product> findAllByProductParentCateoryId(Long category);

    List<Product> findAllByProductCategoryId(Long category);

    List<Product> findAllByProductSubCategoryId(Long category);


}
