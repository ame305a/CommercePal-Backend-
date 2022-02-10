package com.commerce.pal.backend.repo.user;

import com.commerce.pal.backend.models.user.Messenger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessengerRepository extends JpaRepository<Messenger, Long> {

    Optional<Messenger> findMessengerByEmailAddress(String email);

    Optional<Messenger> findMessengerByMessengerId(Long id);

    List<Messenger> findMessengersByOwnerIdAndOwnerType(Integer owner, String ownerType);

    Optional<Messenger> findMessengerByOwnerIdAndOwnerTypeAndMessengerId(Integer owner, String ownerType, Long id);
}
