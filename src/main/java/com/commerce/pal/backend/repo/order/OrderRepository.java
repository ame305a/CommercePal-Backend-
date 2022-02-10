package com.commerce.pal.backend.repo.order;

import com.commerce.pal.backend.models.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findOrderByOrderRef(String ref);
}
