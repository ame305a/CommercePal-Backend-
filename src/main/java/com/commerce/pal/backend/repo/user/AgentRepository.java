package com.commerce.pal.backend.repo.user;

import com.commerce.pal.backend.models.user.Agent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    Optional<Agent> findAgentByEmailAddress(String email);

    Optional<Agent> findAgentByEmailAddressOrOwnerPhoneNumber(String email, String ownerPhoneNumber);

    Optional<Agent> findAgentByAgentId(Long id);

    List<Agent> findAgentsByOwnerIdAndOwnerType(Integer owner, String ownerType);

    Optional<Agent> findAgentByOwnerIdAndOwnerTypeAndAgentId(Integer owner, String ownerType, Long id);

    Integer countByOwnerIdAndOwnerType(Integer owner, String ownerType);

    @Query(value = "SELECT * FROM Agent a WHERE 1=1 " +
            "AND (:searchKeyword IS NULL OR LOWER(a.AgentName) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) " +
            "AND (:startDate IS NULL OR a.CreatedDate BETWEEN CONVERT(date, :startDate) AND CONVERT(date, :endDate)) " +
            "AND (:status IS NULL OR a.Status = :status) " +
            "AND (:city IS NULL OR a.City = :city) " +
            "AND (:regionId IS NULL OR a.RegionId = :regionId)",
            nativeQuery = true)
    Page<Agent> findByFilterAndDateAndStatus(
            @Param("searchKeyword") String searchKeyword,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("status") Integer status,
            @Param("city") Integer city,
            @Param("regionId") Integer regionId,
            Pageable pageable);

    boolean existsByReferralCode(String referralCode);

    Optional<Agent> findByReferralCode(String referralCode);
}
