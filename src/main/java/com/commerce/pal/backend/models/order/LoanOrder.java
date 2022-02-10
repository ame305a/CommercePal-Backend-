package com.commerce.pal.backend.models.order;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class LoanOrder {
    @Id@Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Basic@Column(name = "OrderId")
    private long orderId;
    @Basic@Column(name = "CustomerId")
    private long customerId;
    @Basic@Column(name = "CustomerPhone")
    private String customerPhone;
    @Basic@Column(name = "CustomerEmail")
    private String customerEmail;
    @Basic@Column(name = "Amount")
    private BigDecimal amount;
    @Basic@Column(name = "Status")
    private Integer status;
    @Basic@Column(name = "CreatedDate")
    private Timestamp createdDate;
    @Basic@Column(name = "ReviewBy")
    private String reviewBy;
    @Basic@Column(name = "ReviewComment")
    private String reviewComment;
    @Basic@Column(name = "ReviewDate")
    private Timestamp reviewDate;

}
