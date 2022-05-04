package com.commerce.pal.backend.repo.setting;

import com.commerce.pal.backend.models.setting.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {
    List<PaymentMethod> findPaymentMethodByStatus(Integer status);
}
