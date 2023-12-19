package com.commerce.pal.backend.repo.product.categories;

import com.commerce.pal.backend.models.product.categories.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findProductCategoriesByParentCategoryId(Long id);

    @Query(value = "SELECT * FROM ProductCategory pc WHERE 1=1 " +
            "AND (:filterByParentCategory IS NULL OR pc.ParentCategoryId = :filterByParentCategory) " +
            "AND (:searchKeyword IS NULL OR LOWER(pc.CategoryName) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) " +
            "AND (:startDate IS NULL OR pc.CreatedDate BETWEEN CONVERT(date, :startDate) AND CONVERT(date, :endDate)) " +
            "AND (:status IS NULL OR pc.Status = :status)",
            nativeQuery = true)
    Page<ProductCategory> findByFilterAndDateAndStatus(
            @Param("filterByParentCategory") Long filterByParentCategory,
            @Param("searchKeyword") String searchKeyword,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("status") Integer status,
            Pageable pageable);


}
