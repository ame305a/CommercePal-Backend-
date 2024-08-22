package com.commerce.pal.backend.models.order.orderFailureReason;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "OrderFailureCategory")
@Data
public class OrderFailureCategory {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "Type")
    private String type;

    @Column(name = "Status")
    private Integer status;

    @Column(name = "CreatedDate")
    private Timestamp createdDate;

    @Column(name = "UpdatedDate")
    private Timestamp updatedDate;
}
