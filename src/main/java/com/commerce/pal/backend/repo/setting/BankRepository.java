package com.commerce.pal.backend.repo.setting;

import com.commerce.pal.backend.models.setting.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<Bank, String> {
}
