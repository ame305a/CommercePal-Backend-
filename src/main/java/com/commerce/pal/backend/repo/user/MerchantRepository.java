package com.commerce.pal.backend.repo.user;

import com.commerce.pal.backend.models.product.Product;
import com.commerce.pal.backend.models.user.Merchant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface MerchantRepository extends JpaRepository<Merchant, Long>, JpaSpecificationExecutor<Merchant> {

    Optional<Merchant> findMerchantByEmailAddress(String email);

    Optional<Merchant> findMerchantByEmailAddressOrOwnerPhoneNumber(String email, String ownerPhoneNumber);

    Optional<Merchant> findMerchantByMerchantId(Long id);

    List<Merchant> findMerchantByCity(Integer cityId);

    List<Merchant> findMerchantsByOwnerIdAndOwnerType(Integer owner, String ownerType);

    List<Merchant> findMerchantsByOwnerIdAndOwnerTypeOrderByCreatedDateDesc(Integer owner, String ownerType);

    Optional<Merchant> findMerchantByOwnerIdAndOwnerTypeAndMerchantId(Integer owner, String ownerType, Long id);

    Integer countByOwnerIdAndOwnerType(Integer owner, String ownerType);

    @Query(value = "SELECT * FROM Merchant m WHERE 1=1 " +
            "AND (:searchKeyword IS NULL OR LOWER(m.MerchantName) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) " +
            "AND (:startDate IS NULL OR m.CreatedDate BETWEEN CONVERT(date, :startDate) AND CONVERT(date, :endDate)) " +
            "AND (:status IS NULL OR m.Status = :status)" +
            "AND (:city IS NULL OR m.City = :city)" +
            "AND (:regionId IS NULL OR m.RegionId = :regionId)",
            nativeQuery = true)
    Page<Merchant> findByFilterAndDateAndStatus(
            @Param("searchKeyword") String searchKeyword,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("status") Integer status,
            @Param("city") Integer city,
            @Param("regionId") Integer regionId,
            Pageable pageable);

}

