package com.commerce.pal.backend.models.order;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "OrderFeedback")
public class OrderFeedback {

    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "OrderId")
    private Long orderId;

    @Column(name = "CustomerId")
    private Long customerId;

    @Column(name = "FailureReasonId")
    private Long failureReasonId;

    @Column(name = "FeedbackText", length = 500)
    private String feedbackText;

    @Column(name = "FeedbackDate")
    private Timestamp feedbackDate;

    @Column(name = "Status")
    private Integer status;
}
