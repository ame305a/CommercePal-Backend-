package com.commerce.pal.backend.models.product;

import lombok.*;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class ProductFeature {
    @Id@Column(name = "Id")
    private long id;
    @Basic@Column(name = "FeatureName")
    private String featureName;
    @Basic@Column(name = "UnitOfMeasure")
    private String unitOfMeasure;
    @Basic@Column(name = "VariableType")
    private int variableType;
    @Basic@Column(name = "Status")
    private int status;
    @Basic@Column(name = "CreatedDate")
    private Timestamp createdDate;

}
