package com.commerce.pal.backend.repo.transaction;

import com.commerce.pal.backend.models.transaction.MerchantWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantWithdrawalRepository extends JpaRepository<MerchantWithdrawal, Long> {
}
