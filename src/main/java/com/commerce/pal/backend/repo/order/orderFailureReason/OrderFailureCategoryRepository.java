package com.commerce.pal.backend.repo.order.orderFailureReason;

import com.commerce.pal.backend.models.order.orderFailureReason.OrderFailureCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OrderFailureCategoryRepository extends JpaRepository<OrderFailureCategory, Long> {

    List<OrderFailureCategory> findByStatus(Integer status);
}