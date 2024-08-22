package com.commerce.pal.backend.models.product;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Entity
public class ProductVariant {
    @Id
    @Column(name = "ProductVariantId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productVariantId;

    @Column(name = "ProductId")
    private Long productId;

    @Column(name = "SKU")
    private String sku;

    @Column(name = "UnitPrice")
    private BigDecimal unitPrice;

    @Column(name = "Quantity")
    private Integer quantity;

    @Column(name = "ProductVariantImage")
    private String productVariantImage;

    @Column(name = "IsDiscounted")
    private Integer isDiscounted;

    @Column(name = "DiscountType")
    private String discountType;

    @Column(name = "DiscountValue")
    private BigDecimal discountValue;

    @Column(name = "DiscountExpiryDate")
    private Timestamp discountExpiryDate;

    @Column(name = "IsPromoted")
    private Integer isPromoted;

    @Column(name = "IsPrioritized")
    private Integer isPrioritized;

    @Column(name = "Status")
    private Integer status;

    @Column(name = "StatusComment")
    private String statusComment;

    @Column(name = "CreatedDate")
    private Timestamp createdDate;

    @Column(name = "StatusUpdatedDate")
    private Timestamp statusUpdatedDate;

}