package com.commerce.pal.backend.repo.order;

import com.commerce.pal.backend.models.order.SpecialProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecialProductOrderRepository extends JpaRepository<SpecialProductOrder, Long> {

    List<SpecialProductOrder> findSpecialProductOrdersByUserTypeAndUserId(String type, Long id);
}
