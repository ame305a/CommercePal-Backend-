package com.commerce.pal.backend.repo.user;

import com.commerce.pal.backend.models.user.Distributor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DistributorRepository extends JpaRepository<Distributor, Long> {
    Optional<Distributor> findDistributorByEmailAddress(String email);

    Optional<Distributor> findDistributorByDistributorId(Long distributor);
}
