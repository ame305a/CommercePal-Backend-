package com.commerce.pal.backend.repo.transaction;

import com.commerce.pal.backend.models.transaction.MerchantWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantWithdrawalRepository extends JpaRepository<MerchantWithdrawal, Long> {

    Optional<MerchantWithdrawal> findMerchantWithdrawalByMerchantIdAndStatus(Long merchant, Integer status);
}
