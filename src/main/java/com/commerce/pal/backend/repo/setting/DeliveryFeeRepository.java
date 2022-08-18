package com.commerce.pal.backend.repo.setting;

import com.commerce.pal.backend.models.setting.DeliveryFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryFeeRepository extends JpaRepository<DeliveryFee, Integer> {

    Optional<DeliveryFee> findDeliveryFeeByDeliveryTypeAndCustomerType(String delivery,String customer);
}
