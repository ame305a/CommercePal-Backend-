package com.commerce.pal.backend.repo.transaction;

import com.commerce.pal.backend.models.transaction.AgentFloat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.List;

public interface AgentFloatRepository extends JpaRepository<AgentFloat, Integer> {
    List<AgentFloat> findAgentFloatsByStatusOrderByRequestDate(Integer status);

    @Query(value = "SELECT * FROM AgentFloat af WHERE 1=1 " +
            "AND (:startDate IS NULL OR af.RequestDate BETWEEN CONVERT(date, :startDate) AND CONVERT(date, :endDate)) " +
            "AND (:status IS NULL OR af.Status = :status)",
            nativeQuery = true)
    Page<AgentFloat> findByDateAndStatus(Integer status, Timestamp startDate, Timestamp endDate, Pageable pageable);

}
