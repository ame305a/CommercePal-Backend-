package com.commerce.pal.backend.models.product;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table
public class ProductVariantSpecification {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ProductId")
    private Long productId;

    @Column(name = "ProductVariantId")
    private Long productVariantId;

    @Column(name = "ProductFeatureId")
    private Long productFeatureId;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Value", nullable = false)
    private String value;

    @Column(name = "UnitOfMeasure")
    private String unitOfMeasure;

    @Column(name = "Status")
    private Integer status;

    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
