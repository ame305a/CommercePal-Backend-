package com.commerce.pal.backend.repo.order;

import com.commerce.pal.backend.models.order.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findOrderByOrderRef(String ref);

    @Query(value = "SELECT * FROM [Order] o WHERE 1=1 " +
            "AND (:startDate IS NULL OR o.OrderDate BETWEEN CONVERT(date, :startDate) AND CONVERT(date, :endDate)) " +
            "AND (:status IS NULL OR o.Status = :status)" +
            "AND (:paymentStatus IS NULL OR o.PaymentStatus = :paymentStatus)" +
            "AND (:shippingStatus IS NULL OR o.ShippingStatus = :shippingStatus)",
            nativeQuery = true)
    Page<Order> findByDateAndStatus(Integer status, Integer paymentStatus, Integer shippingStatus, Timestamp startDate, Timestamp endDate, Pageable pageable);
}
