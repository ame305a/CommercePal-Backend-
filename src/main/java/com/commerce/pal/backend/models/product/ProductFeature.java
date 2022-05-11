package com.commerce.pal.backend.models.product;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class ProductFeature {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Basic
    @Column(name = "SubCategoryId")
    private Long subCategoryId;
    @Basic
    @Column(name = "FeatureName")
    private String featureName;
    @Basic
    @Column(name = "UnitOfMeasure")
    private String unitOfMeasure;
    @Basic
    @Column(name = "VariableType")
    private String variableType;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
