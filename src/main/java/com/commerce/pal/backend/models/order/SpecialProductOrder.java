package com.commerce.pal.backend.models.order;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Entity
public class SpecialProductOrder {
    @Id
    @Column(name = "Id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Basic
    @Column(name = "UserType")
    private String userType;
    @Basic
    @Column(name = "UserId")
    private Long userId;
    @Basic
    @Column(name = "ProductSubCategoryId")
    private Long productSubCategoryId;
    @Basic
    @Column(name = "ProductName")
    private String productName;
    @Basic
    @Column(name = "ProductDescription")
    private String productDescription;
    @Basic
    @Column(name = "Quantity")
    private Integer quantity;
    @Basic
    @Column(name = "EstimatePrice")
    private BigDecimal estimatePrice;
    @Basic
    @Column(name = "LinkToProduct")
    private String linkToProduct;
    @Basic
    @Column(name = "ImageOne")
    private String imageOne;
    @Basic
    @Column(name = "ImageTwo")
    private String imageTwo;
    @Basic
    @Column(name = "ImageThree")
    private String imageThree;
    @Basic
    @Column(name = "ImageFour")
    private String imageFour;
    @Basic
    @Column(name = "ImageFive")
    private String imageFive;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "RequestDate")
    private Timestamp requestDate;
    @Basic
    @Column(name = "Review")
    private String review;
    @Basic
    @Column(name = "ProductId")
    private Integer productId;
    @Basic
    @Column(name = "UploadedBy")
    private Integer uploadedBy;
    @Basic
    @Column(name = "UploadedDate")
    private Timestamp uploadedDate;

}
