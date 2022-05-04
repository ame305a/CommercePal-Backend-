package com.commerce.pal.backend.models.setting;

import lombok.*;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class PaymentMethod {
    @Id
    @Column(name = "Id")
    private Integer id;
    @Basic
    @Column(name = "Name")
    private String name;
    @Basic
    @Column(name = "PaymentMethod")
    private String paymentMethod;
    @Basic
    @Column(name = "IconUrl")
    private String iconUrl;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
