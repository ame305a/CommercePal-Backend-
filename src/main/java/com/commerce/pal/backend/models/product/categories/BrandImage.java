package com.commerce.pal.backend.models.product.categories;

import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
@Table(name = "BrandImages")
public class BrandImage {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Basic
    @Column(name = "ParentCategoryId")
    private Long parentCategoryId;
    @Basic
    @Column(name = "Brand")
    private String brand;
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
