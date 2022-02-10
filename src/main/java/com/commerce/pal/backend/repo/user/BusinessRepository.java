package com.commerce.pal.backend.repo.user;

import com.commerce.pal.backend.models.user.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findBusinessByEmailAddress(String email);

    Optional<Business> findBusinessByBusinessId(Long id);

    List<Business> findBusinessByOwnerIdAndOwnerType(Integer owner, String ownerType);

    Optional<Business> findBusinessByOwnerIdAndOwnerTypeAndBusinessId(Integer owner, String ownerType, Long id);

    Integer countByOwnerIdAndOwnerType(Integer owner, String ownerType);
}
