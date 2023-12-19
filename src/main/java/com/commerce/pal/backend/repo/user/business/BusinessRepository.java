package com.commerce.pal.backend.repo.user.business;

import com.commerce.pal.backend.models.user.business.Business;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findBusinessByEmailAddress(String email);

    Optional<Business> findBusinessByBusinessId(Long id);

    List<Business> findBusinessByOwnerIdAndOwnerType(Integer owner, String ownerType);

    Optional<Business> findBusinessByOwnerIdAndOwnerTypeAndBusinessId(Integer owner, String ownerType, Long id);

    Integer countByOwnerIdAndOwnerType(Integer owner, String ownerType);

    Optional<Business> findBusinessByBusinessIdAndEmailAddress(Long id, String email);

    List<Business> findBusinessByFinancialInstitution(Integer finance);

    @Query(value = "SELECT * FROM Business b WHERE 1=1 " +
            "AND (:searchKeyword IS NULL OR LOWER(b.BusinessName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(b.Country) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR LOWER(b.City) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) " +
            "AND (:startDate IS NULL OR b.CreatedDate BETWEEN CONVERT(date, :startDate) AND CONVERT(date, :endDate)) " +
            "AND (:status IS NULL OR b.Status = :status)",
            nativeQuery = true)
    Page<Business> findByFilterAndDateAndStatus(
            @Param("searchKeyword") String searchKeyword,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("status") Integer status,
            Pageable pageable);
}
