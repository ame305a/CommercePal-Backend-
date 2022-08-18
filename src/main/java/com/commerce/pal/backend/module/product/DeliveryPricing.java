package com.commerce.pal.backend.module.product;

import com.commerce.pal.backend.repo.setting.DeliveryFeeRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log
@Service
@SuppressWarnings("Duplicates")
public class DeliveryPricing {
    private final DeliveryFeeRepository deliveryFeeRepository;

    @Autowired
    public DeliveryPricing(DeliveryFeeRepository deliveryFeeRepository) {
        this.deliveryFeeRepository = deliveryFeeRepository;
    }
}
