package com.commerce.pal.backend.models.product;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
@Table(name = "ProductMoreTemplate")
public class ProductMoreTemplate {
    @Id
    @Column(name = "Id")
    private Integer id;
    @Basic
    @Column(name = "ProductMoreId")
    private Integer productMoreId;
    @Basic
    @Column(name = "Type")
    private String type;
    @Basic
    @Column(name = "TypeId")
    private Integer typeId;
    @Basic
    @Column(name = "Description")
    private String description;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
