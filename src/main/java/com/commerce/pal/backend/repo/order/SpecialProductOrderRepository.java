package com.commerce.pal.backend.repo.order;

import com.commerce.pal.backend.models.order.SpecialProductOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface SpecialProductOrderRepository extends JpaRepository<SpecialProductOrder, Long> {

    List<SpecialProductOrder> findSpecialProductOrdersByUserTypeAndUserId(String type, Long id);

    @Query(value = "SELECT * FROM SpecialProductOrder sp WHERE 1=1 " +
            "AND (:searchKeyword IS NULL OR LOWER(sp.ProductName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(sp.ProductDescription) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) " +
            "AND (:startDate IS NULL OR sp.RequestDate BETWEEN CONVERT(date, :startDate) AND CONVERT(date, :endDate)) " +
            "AND (:status IS NULL OR sp.Status = :status)",
            nativeQuery = true)
    Page<SpecialProductOrder> findByFilterAndDateAndStatus(
            @Param("searchKeyword") String searchKeyword,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("status") Integer status,
            Pageable pageable);

}
