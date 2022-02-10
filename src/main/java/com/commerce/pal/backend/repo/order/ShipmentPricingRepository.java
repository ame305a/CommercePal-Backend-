package com.commerce.pal.backend.repo.order;

import com.commerce.pal.backend.models.order.ShipmentPricing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentPricingRepository extends JpaRepository<ShipmentPricing, Integer> {
    Optional<ShipmentPricing> findShipmentPricingByShipmentType(String type);

    Optional<ShipmentPricing> findShipmentPricingByShipmentTypeAndSourceAndDestination(String type, Integer source, Integer dest);
}
