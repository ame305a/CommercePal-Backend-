package com.commerce.pal.backend.repo;

import com.commerce.pal.backend.models.LoginValidation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginValidationRepository extends JpaRepository<LoginValidation, Long> {

    Optional<LoginValidation> findLoginValidationByEmailAddressAndPinHash(String email, String hash);

    Optional<LoginValidation> findLoginValidationByEmailAddress(String email);

    LoginValidation findByEmailAddress(String email);

    Optional<LoginValidation> findLoginValidationByEmailAddressAndPasswordResetTokenStatus(String email, Integer status);
}
