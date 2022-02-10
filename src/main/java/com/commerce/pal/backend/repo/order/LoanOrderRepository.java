package com.commerce.pal.backend.repo.order;

import com.commerce.pal.backend.models.order.LoanOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanOrderRepository extends JpaRepository<LoanOrder, Long> {

    Optional<LoanOrder> findLoanOrderByOrderId(Long order);
}
