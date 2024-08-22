package com.commerce.pal.backend.repo.user;

import com.commerce.pal.backend.models.user.Distributor;
import com.commerce.pal.backend.models.user.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.Optional;

public interface DistributorRepository extends JpaRepository<Distributor, Long>, JpaSpecificationExecutor<Distributor> {
    Optional<Distributor> findDistributorByEmailAddress(String email);

    Optional<Distributor> findDistributorByEmailAddressOrPhoneNumber(String email, String ownerPhoneNumber);

    Optional<Distributor> findDistributorByDistributorId(Long distributor);

    @Query(value = "SELECT * FROM Distributor d " +
            "WHERE (:searchKeyword IS NULL OR LOWER(d.DistributorName) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) " +
            "AND (:startDate IS NULL OR d.CreatedDate BETWEEN CONVERT(date, :startDate) AND CONVERT(date, :endDate)) " +
            "AND (:status IS NULL OR d.Status = :status)" +
            "AND (:city IS NULL OR d.City = :city)",
            nativeQuery = true)
    Page<Distributor> findBySearchKeywordAndDateAndStatus(
            @Param("searchKeyword") String searchKeyword,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("status") Integer status,
            @Param("city") String city,
            Pageable pageable);

}
