package com.commerce.pal.backend.models.product;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
@Table(name = "ProductCategory")
public class ProductCategory {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Basic
    @Column(name = "ParentCategoryId")
    private long parentCategoryId;
    @Basic
    @Column(name = "CategoryType")
    private String categoryType;
    @Basic
    @Column(name = "CategoryName")
    private String categoryName;
    @Basic
    @Column(name = "CategoryMobileImage")
    private String categoryMobileImage;
    @Basic
    @Column(name = "CategoryWebImage")
    private String categoryWebImage;
    @Basic
    @Column(name = "WebThumbnail")
    private String webThumbnail;
    @Basic
    @Column(name = "MobileThumbnail")
    private String mobileThumbnail;
    @Basic
    @Column(name = "Status")
    private int status;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

}
