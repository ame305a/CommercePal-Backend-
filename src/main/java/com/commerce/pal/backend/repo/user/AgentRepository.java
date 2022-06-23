package com.commerce.pal.backend.repo.user;

import com.commerce.pal.backend.models.user.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    Optional<Agent> findAgentByEmailAddress(String email);

    Optional<Agent> findAgentByAgentId(Long id);

    List<Agent> findAgentsByOwnerIdAndOwnerType(Integer owner, String ownerType);

    Optional<Agent> findAgentByOwnerIdAndOwnerTypeAndAgentId(Integer owner, String ownerType, Long id);

    Integer countByOwnerIdAndOwnerType(Integer owner, String ownerType);

}
