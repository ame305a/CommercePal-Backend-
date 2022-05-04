package com.commerce.pal.backend.repo.setting;

import com.commerce.pal.backend.models.setting.PaymentMethodItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentMethodItemRepository extends JpaRepository<PaymentMethodItem, Integer> {

    List<PaymentMethodItem> findPaymentMethodItemsByPaymentMethodIdAndStatus(Integer method, Integer status);
}
