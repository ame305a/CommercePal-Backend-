package com.commerce.pal.backend.repo.user.invitation;

import com.commerce.pal.backend.models.user.invitation.UserInvitation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInvitationRepository extends JpaRepository<UserInvitation, Long> {
    Optional<UserInvitation> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);

}

