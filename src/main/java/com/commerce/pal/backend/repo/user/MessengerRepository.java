package com.commerce.pal.backend.repo.user;

import com.commerce.pal.backend.models.user.Messenger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface MessengerRepository extends JpaRepository<Messenger, Long> {

    Optional<Messenger> findMessengerByEmailAddress(String email);

    Optional<Messenger> findMessengerByMessengerId(Long id);

    List<Messenger> findMessengersByOwnerIdAndOwnerType(Integer owner, String ownerType);

    Optional<Messenger> findMessengerByOwnerIdAndOwnerTypeAndMessengerId(Integer owner, String ownerType, Long id);

    @Query("SELECT m FROM Messenger m WHERE 1=1 " +
            "AND (:startDate IS NULL OR m.createdDate BETWEEN :startDate AND :endDate) " +
            "AND (:status IS NULL OR m.status = :status)")
    Page<Messenger> findByStartDateBetweenAndStatus(
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("status") Integer status,
            Pageable pageable);
}
