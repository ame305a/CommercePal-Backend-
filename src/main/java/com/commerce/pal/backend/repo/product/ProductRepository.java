package com.commerce.pal.backend.repo.product;

import com.commerce.pal.backend.models.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findProductByProductId(Long product);

    Optional<Product> findProductByOwnerTypeAndMerchantIdAndProductId(String type, Long merchant, Long product);

    List<Product> findByOwnerTypeAndMerchantId(String type, Long merchant);

    @Query(value = "SELECT ProductParentCateoryId  FROM Product WHERE MerchantId = ?1  GROUP BY ProductParentCateoryId", nativeQuery = true)
    List<Long> findProductsByProductParentCateoryId(Long merchant);

    @Query(value = "SELECT ProductCategoryId  FROM Product WHERE MerchantId = :merchant AND ProductParentCateoryId = :parent GROUP BY ProductCategoryId", nativeQuery = true)
    List<Long> findProductsByProductCategoryId(Long merchant, Long parent);

    @Query(value = "SELECT ProductSubCategoryId  FROM Product WHERE MerchantId = :merchant AND ProductCategoryId = :category  GROUP BY ProductSubCategoryId", nativeQuery = true)
    List<Long> findProductsByProductSubCategoryId(Long merchant, Long category);

    @Query(value = "SELECT *  FROM Product WHERE " +
            "(ProductName LIKE %:productName% OR " +
            "ShortDescription LIKE %:shortDes% OR " +
            "ProductDescription LIKE %:productDes% OR " +
            "SpecialInstruction LIKE %:prodIns%) AND " +
            "Status = 1 AND ProductType = :type", nativeQuery = true)
    List<Product> findProductByProductId(String productName, String shortDes, String productDes, String prodIns, String type);

    @Query(value = "SELECT * FROM Product p WHERE 1=1 " +
            "AND (:subCategoryId IS NULL OR p.ProductSubCategoryId = :subCategoryId) " +
            "AND (p.UnitPrice >= :minPrice AND p.UnitPrice <= :maxPrice) " +
            "AND p.Status = 1 AND p.ProductType = :type",
            nativeQuery = true)
    Page<Product> findByPriceRange(
            @Param("subCategoryId") Long subCategoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("type") String type,
            Pageable pageable);

    @Query(value = "SELECT * FROM Product p WHERE 1=1 " +
            "AND (:category IS NULL OR p.ProductCategoryId = :category) " +
            "AND (:parentCategory IS NULL OR p.ProductParentCateoryId = :parentCategory) " +
            "AND (:productType IS NULL OR p.ProductType = :productType) " +
            "AND (:searchKeyword IS NULL OR LOWER(p.ProductName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(p.ShortDescription) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) " +
            "AND (:startDate IS NULL OR p.CreatedDate BETWEEN CONVERT(date, :startDate) AND CONVERT(date, :endDate)) " +
            "AND (:status IS NULL OR p.Status = :status)" +
            "AND (:merchantId IS NULL OR p.MerchantId = :merchantId)" +
            "AND (:city is NULL OR p.MerchantId IN :cityMerchantIds)",
            nativeQuery = true)
    Page<Product> findByFilterAndMerchantAndDateAndStatus(
            @Param("category") Long category,
            @Param("parentCategory") Long parentCategory,
            @Param("productType") String productType,
            @Param("searchKeyword") String searchKeyword,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("status") Integer status,
            @Param("merchantId") Long merchantId,
            @Param("city") Integer city,
            @Param("cityMerchantIds") List<Long> cityMerchantIds,
            Pageable pageable);

    @Query(value = "SELECT * FROM Product p WHERE 1=1 " +
            "AND (:subCategoryId IS NULL OR p.ProductSubCategoryId = :subCategoryId) " +
            "AND (:timestamp IS NULL OR p.CreatedDate >= CONVERT(date, :timestamp)) " +
            "AND p.Status = 1 AND p.ProductType = :type",
            nativeQuery = true)
    Page<Product> findNewlyAddedProducts(
            @Param("subCategoryId") Long subCategoryId,
            @Param("timestamp") Timestamp timestamp,
            @Param("type") String type,
            Pageable pageable);

    @Query(value = "SELECT * FROM (SELECT *, ROW_NUMBER() OVER (ORDER BY NEWID()) AS rn FROM Product WHERE UnitPrice < :priceThreshold AND Quantity > 0 AND Status = 1) AS sub WHERE rn <= :count", nativeQuery = true)
    List<Product> findRandomProductsUnderPrice(@Param("priceThreshold") BigDecimal priceThreshold, @Param("count") int count);

    @Query(value = "SELECT * FROM (SELECT *, ROW_NUMBER() OVER (ORDER BY NEWID()) AS rn FROM Product WHERE UnitPrice > :priceThreshold AND Quantity > 0 AND Status = 1) AS sub WHERE rn <= :count", nativeQuery = true)
    List<Product> findRandomProductsAbovePrice(@Param("priceThreshold") BigDecimal priceThreshold, @Param("count") int count);

    @Query(value = "SELECT * FROM Product p " +
            "WHERE p.IsProductOnFlashSale = 1 " +
            "AND p.Status = 1 " +
            "AND LOWER(p.ProductType) = LOWER(:type)",
            nativeQuery = true)
    Page<Product> findProductsOnFlashSale(@Param("type") String type, Pageable pageable);


    //TODO: consider case where is DiscountExpiryDate
    @Query(value = "SELECT * FROM Product p " +
            "WHERE p.IsDiscounted = 1 " +
            "AND p.DiscountValue > 0 " +
            "AND p.Status = 1 " +
            "AND LOWER(p.ProductType) = LOWER(:type)",
            nativeQuery = true)
    Page<Product> findTopDealProducts(@Param("type") String type, Pageable pageable);

    List<Product> findByOwnerTypeAndMerchantIdAndSoldQuantityGreaterThan(String ownerType, Long merchantId, Integer soldQuantity);

    List<Product> findByOwnerTypeAndMerchantIdAndIsDiscountedAndDiscountValueGreaterThan(String ownerType, Long merchantId, Integer isDiscounted, BigDecimal discountValue);

    List<Product> findByOwnerTypeAndMerchantIdAndStatus(String ownerType, Long merchantId, Integer status);
}
