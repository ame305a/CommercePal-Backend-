package com.commerce.pal.backend.repo;

import com.commerce.pal.backend.models.LoginValidation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginValidationRepository extends JpaRepository<LoginValidation, Long> {

    Optional<LoginValidation> findLoginValidationByEmailAddressAndPinHash(String email, String hash);

    Optional<LoginValidation> findLoginValidationByEmailAddress(String email);

    Optional<LoginValidation> findLoginValidationByEmailAddressOrPhoneNumber(String email, String phone);

    LoginValidation findByEmailAddress(String email);

    LoginValidation findByEmailAddressOrPhoneNumber(String email, String phone);

    Optional<LoginValidation> findLoginValidationByEmailAddressAndPasswordResetTokenStatus(String email, Integer status);

    Optional<LoginValidation> findLoginValidationByEmailAddressOrPhoneNumberAndPasswordResetTokenStatus(String email, String phone, Integer status);

    Optional<LoginValidation> findLoginValidationByPhoneNumber(String phone);
}
