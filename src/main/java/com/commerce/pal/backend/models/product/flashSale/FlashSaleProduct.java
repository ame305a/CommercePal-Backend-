package com.commerce.pal.backend.models.product.flashSale;

import com.commerce.pal.backend.models.product.SubProduct;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
@Entity
@Table(name = "FlashSaleProduct")
public class FlashSaleProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "SubProduct")
    private SubProduct subProduct;

    @Column(name = "MerchantId")
    private Long merchantId;

    @Column(name = "FlashSalePrice")
    private BigDecimal flashSalePrice;

    @Column(name = "FlashSaleStartDate")
    private Timestamp flashSaleStartDate;

    @Column(name = "FlashSaleEndDate")
    private Timestamp flashSaleEndDate;

    @Column(name = "FlashSaleInventoryQuantity")
    private Integer flashSaleInventoryQuantity;

    @Column(name = "FlashSaleTotalQuantitySold")
    private Integer flashSaleTotalQuantitySold;

    @Column(name = "IsQuantityRestrictedPerCustomer")
    private Boolean isQuantityRestrictedPerCustomer;

    @Column(name = "FlashSaleMinQuantityPerCustomer")
    private Integer flashSaleMinQuantityPerCustomer;

    @Column(name = "FlashSaleMaxQuantityPerCustomer")
    private Integer flashSaleMaxQuantityPerCustomer;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private FlashSaleStatus status = FlashSaleStatus.PENDING;

    @CreationTimestamp
    @Column(name = "CreatedDate")
    private Timestamp createdDate;

    @UpdateTimestamp
    @Column(name = "UpdatedDate")
    private Timestamp updatedDate;
}

