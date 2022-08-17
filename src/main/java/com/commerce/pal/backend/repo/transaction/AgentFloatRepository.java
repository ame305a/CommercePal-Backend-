package com.commerce.pal.backend.repo.transaction;

import com.commerce.pal.backend.models.transaction.AgentFloat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgentFloatRepository extends JpaRepository<AgentFloat, Integer> {
    List<AgentFloat> findAgentFloatsByStatusOrderByRequestDate(Integer status);
}
