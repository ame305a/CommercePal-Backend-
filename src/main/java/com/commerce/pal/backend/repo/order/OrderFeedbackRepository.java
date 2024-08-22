package com.commerce.pal.backend.repo.order;

import com.commerce.pal.backend.models.order.OrderFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public interface OrderFeedbackRepository extends JpaRepository<OrderFeedback, Long> {

    @Query(value = "SELECT * FROM OrderFeedback o WHERE 1=1 " +
            "AND (:startDate IS NULL OR o.FeedbackDate BETWEEN CONVERT(date, :startDate) AND CONVERT(date, :endDate)) ",
            nativeQuery = true)
    Page<OrderFeedback> findByFeedbackDateBetween(Timestamp startDate, Timestamp endDate, Pageable pageable);
}
