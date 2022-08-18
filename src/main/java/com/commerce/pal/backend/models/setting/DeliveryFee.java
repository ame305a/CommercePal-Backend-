package com.commerce.pal.backend.models.setting;

import lombok.*;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class DeliveryFee {
    @Id
    @Column(name = "id")
    private Integer id;
    @Basic
    @Column(name = "delivery_type")
    private String deliveryType;
    @Basic
    @Column(name = "customer_type")
    private String customerType;
    @Basic
    @Column(name = "amount")
    private BigDecimal amount;
    @Basic
    @Column(name = "status")
    private Integer status;
    @Basic
    @Column(name = "created_date")
    private Timestamp createdDate;

}
