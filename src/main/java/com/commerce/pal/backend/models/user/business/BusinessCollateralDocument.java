package com.commerce.pal.backend.models.user.business;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class BusinessCollateralDocument {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic
    @Column(name = "CollateralId")
    private Integer collateralId;
    @Basic
    @Column(name = "DocumentUrl")
    private String documentUrl;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
