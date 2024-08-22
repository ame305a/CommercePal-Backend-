package com.commerce.pal.backend.models.order.orderFailureReason;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "OrderFailureReason")
public class OrderFailureReason {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "OrderFailureCategory")
    private Long orderFailureCategory;

    @Column(name = "Reason")
    private String reason;

    @Column(name = "Status")
    private Integer status;

    @Column(name = "CreatedDate")
    private Timestamp createdDate;

    @Column(name = "UpdatedDate")
    private Timestamp updatedDate;
}
