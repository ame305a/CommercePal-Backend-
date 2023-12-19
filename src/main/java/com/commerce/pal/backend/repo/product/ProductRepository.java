package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findProductByProductId(Long product);

    Optional<Product> findProductByOwnerTypeAndMerchantIdAndProductId(String type, Long merchant, Long product);

    List<Product> findProductsByProductParentCateoryIdAndProductCategoryIdAndProductSubCategoryId(Long par, Long cat, Long sub);

    @Query(value = "SELECT ProductParentCateoryId  FROM Product WHERE MerchantId = ?1  GROUP BY ProductParentCateoryId", nativeQuery = true)
    List<Long> findProductsByProductParentCateoryId(Long merchant);

    @Query(value = "SELECT ProductCategoryId  FROM Product WHERE MerchantId = :merchant AND ProductParentCateoryId = :parent GROUP BY ProductCategoryId", nativeQuery = true)
    List<Long> findProductsByProductCategoryId(Long merchant, Long parent);

    @Query(value = "SELECT ProductSubCategoryId  FROM Product WHERE MerchantId = :merchant AND ProductCategoryId = :category  GROUP BY ProductSubCategoryId", nativeQuery = true)
    List<Long> findProductsByProductSubCategoryId(Long merchant, Long category);

    List<Product> findAllByProductParentCateoryId(Long category);

    List<Product> findAllByProductCategoryId(Long category);

    List<Product> findAllByProductSubCategoryId(Long category);

    @Query(value = "SELECT *  FROM Product WHERE " +
            "(ProductName LIKE %:productName% OR " +
            "ShortDescription LIKE %:shortDes% OR " +
            "ProductDescription LIKE %:productDes% OR " +
            "SpecialInstruction LIKE %:prodIns%) AND Status = 1 AND ProductType = :type", nativeQuery = true)
    List<Product> findProductByProductId(String productName, String shortDes, String productDes, String prodIns, String type);

    @Query(value = "SELECT * FROM Product p WHERE 1=1 " +
            "AND (:filterByCategory IS NULL OR p.ProductCategoryId = :filterByCategory) " +
            "AND (:searchKeyword IS NULL OR LOWER(p.ProductName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(p.ShortDescription) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) " +
            "AND (:startDate IS NULL OR p.CreatedDate BETWEEN CONVERT(date, :startDate) AND CONVERT(date, :endDate)) " +
            "AND (:status IS NULL OR p.Status = :status)" +
            "AND (:merchantId IS NULL OR p.MerchantId = :merchantId)",
            nativeQuery = true)
    Page<Product> findByFilterAndMerchantAndDateAndStatus(
            @Param("filterByCategory") Long filterByCategory,
            @Param("searchKeyword") String searchKeyword,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("status") Integer status,
            @Param("merchantId") Long merchantId,
            Pageable pageable);

}
