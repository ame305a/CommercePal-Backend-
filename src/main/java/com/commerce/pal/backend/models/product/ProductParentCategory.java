package com.commerce.pal.backend.models.product;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
@Table(name = "ProductParentCategory")
public class ProductParentCategory {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Basic
    @Column(name = "ParentCategoryName")
    private String parentCategoryName;
    @Basic
    @Column(name = "ParentCategoryImage")
    private String parentCategoryImage;
    @Basic
    @Column(name = "WebImage")
    private String webImage;
    @Basic
    @Column(name = "MobileImage")
    private String mobileImage;
    @Basic
    @Column(name = "Status")
    private int status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
