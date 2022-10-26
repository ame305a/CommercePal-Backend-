package com.commerce.pal.backend.models.product;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Data
@Entity
@Table(name = "Product")
public class Product {
    @Id
    @Column(name = "ProductId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    @Basic
    @Column(name = "OwnerType")
    private String ownerType;
    @Basic
    @Column(name = "MerchantId")
    private Long merchantId;
    @Basic
    @Column(name = "ProductParentCateoryId")
    private Long productParentCateoryId;
    @Basic
    @Column(name = "ProductCategoryId")
    private Long productCategoryId;
    @Basic
    @Column(name = "ProductSubCategoryId")
    private Long productSubCategoryId;
    @Basic
    @Column(name = "ProductType")
    private String productType;
    @Basic
    @Column(name = "ProductName")
    private String productName;
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
    @Column(name = "ProductDescription")
    private String productDescription;
    @Basic
    @Column(name = "SpecialInstruction")
    private String specialInstruction;
    @Basic
    @Column(name = "Quantity")
    private Integer quantity;
    @Basic
    @Column(name = "UnitOfMeasure")
    private String unitOfMeasure;
    @Basic
    @Column(name = "UnitPrice")
    private BigDecimal unitPrice;
    @Basic
    @Column(name = "Currency")
    private String currency;
    @Basic
    @Column(name = "Tax")
    private BigDecimal tax;
    @Basic
    @Column(name = "MinOrder")
    private Integer minOrder;
    @Basic
    @Column(name = "MaxOrder")
    private Integer maxOrder;
    @Basic
    @Column(name = "SoldQuantity")
    private Integer soldQuantity;
    @Basic
    @Column(name = "CountryOfOrigin")
    private String countryOfOrigin;
    @Basic
    @Column(name = "Manufucturer")
    private String manufucturer;
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
    @Column(name = "UnitWeight")
    private BigDecimal unitWeight;
    @Basic
    @Column(name = "IsPromoted")
    private Integer isPromoted;
    @Basic
    @Column(name = "IsPrioritized")
    private Integer isPrioritized;
    @Basic
    @Column(name = "ShipmentType")
    private String shipmentType;


    @Basic
    @Column(name = "PrimarySubProduct")
    private Long primarySubProduct;

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
