package com.commerce.pal.backend.repo.user;

import com.commerce.pal.backend.models.user.Distributor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.Optional;

public interface DistributorRepository extends JpaRepository<Distributor, Long> {
    Optional<Distributor> findDistributorByEmailAddress(String email);

    Optional<Distributor> findDistributorByDistributorId(Long distributor);

    @Query(value = "SELECT * FROM Distributor d " +
            "WHERE (:searchKeyword IS NULL OR LOWER(d.DistributorName) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) " +
            "AND (:startDate IS NULL OR d.CreatedDate BETWEEN CONVERT(date, :startDate) AND CONVERT(date, :endDate)) " +
            "AND (:status IS NULL OR d.Status = :status)",
            nativeQuery = true)
    Page<Distributor> findBySearchKeywordAndDateAndStatus(
            @Param("searchKeyword") String searchKeyword,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("status") Integer status,
            Pageable pageable);

}
