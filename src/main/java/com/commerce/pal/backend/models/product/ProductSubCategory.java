package com.commerce.pal.backend.models.product;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
@Table(name = "ProductSubCategory")
public class ProductSubCategory {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Basic
    @Column(name = "ProductCategoryId")
    private long productCategoryId;
    @Basic
    @Column(name = "SubCategoryType")
    private String subCategoryType;
    @Basic
    @Column(name = "SubCategoryName")
    private String subCategoryName;
    @Basic
    @Column(name = "WebImage")
    private String webImage;
    @Basic
    @Column(name = "MobileImage")
    private String mobileImage;
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
