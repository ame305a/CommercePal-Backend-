package com.commerce.pal.backend.models.product;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class ProductFeatureValue {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Basic
    @Column(name = "ProductId")
    private Long productId;
    @Basic
    @Column(name = "ProductFeatureId")
    private Long productFeatureId;
    @Basic
    @Column(name = "Value")
    private String value;
    @Basic
    @Column(name = "UnitOfMeasure")
    private String unitOfMeasure;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
