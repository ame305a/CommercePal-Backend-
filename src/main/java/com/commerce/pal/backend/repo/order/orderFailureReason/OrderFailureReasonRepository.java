package com.commerce.pal.backend.repo.order.orderFailureReason;

import com.commerce.pal.backend.models.order.orderFailureReason.OrderFailureReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OrderFailureReasonRepository extends JpaRepository<OrderFailureReason, Long> {

    List<OrderFailureReason> findByOrderFailureCategoryAndStatus(long category, int status);

}