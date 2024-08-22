package com.commerce.pal.backend.dto.product.flashSale;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class FlashSaleDTO {

    @NotNull
    private Long productId;

    @NotNull
    private Long subProductId;

    @Positive
    @NotNull
    private BigDecimal flashSalePrice;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "UTC")
    private Timestamp flashSaleStartDate;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "UTC")
    private Timestamp flashSaleEndDate;

    @NotNull
    @Positive
    private Integer flashSaleInventoryQuantity;

    @NotNull
    private Boolean isQuantityRestrictedPerCustomer;

    private Integer flashSaleMinQuantityPerCustomer;

    private Integer flashSaleMaxQuantityPerCustomer;
}

