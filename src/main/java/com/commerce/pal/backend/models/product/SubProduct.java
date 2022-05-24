package com.commerce.pal.backend.models.product;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
public class SubProduct {
    @Id
    @Column(name = "SubProductId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subProductId;
    @Basic
    @Column(name = "ProductId")
    private Long productId;
    @Basic
    @Column(name = "ShortDescription")
    private String shortDescription;
    @Basic
    @Column(name = "ProductImage")
    private String productImage;
    @Basic
    @Column(name = "ProductMobileImage")
    private String productMobileImage;
    @Basic
    @Column(name = "WebThumbnail")
    private String webThumbnail;
    @Basic
    @Column(name = "MobileThumbnail")
    private String mobileThumbnail;
    @Basic
    @Column(name = "ProductMobileVideo")
    private String productMobileVideo;
    @Basic
    @Column(name = "ProductWebVideo")
    private String productWebVideo;
    @Basic
    @Column(name = "UnitPrice")
    private BigDecimal unitPrice;
    @Basic
    @Column(name = "IsDiscounted")
    private Integer isDiscounted;
    @Basic
    @Column(name = "DiscountType")
    private String discountType;
    @Basic
    @Column(name = "DiscountValue")
    private BigDecimal discountValue;
    @Basic
    @Column(name = "DiscountExpiryDate")
    private Timestamp discountExpiryDate;
    @Basic
    @Column(name = "IsPromoted")
    private Integer isPromoted;
    @Basic
    @Column(name = "IsPrioritized")
    private Integer isPrioritized;
    @Basic
    @Column(name = "Status")
    private Integer status;
    @Basic
    @Column(name = "StatusComment")
    private String statusComment;
    @Basic
    @Column(name = "StatusUpdatedDate")
    private Timestamp statusUpdatedDate;
    @Basic
    @Column(name = "CreatedBy")
    private String createdBy;
    @Basic
    @Column(name = "CreatedDate")
    private Timestamp createdDate;
    @Basic
    @Column(name = "VerifiedBy")
    private String verifiedBy;
    @Basic
    @Column(name = "VerifiedDate")
    private Timestamp verifiedDate;
}
