package com.commerce.pal.backend.dto.product.promotion;

import com.commerce.pal.backend.models.product.promotion.DiscountType;
import com.commerce.pal.backend.models.product.promotion.ProductPromoCodeOwner;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class ProductPromoCodeDTO {
    @NotNull
    private ProductPromoCodeOwner owner;

    @NotBlank
    private String code;

    @NotBlank
    private String promoCodeDescription;

    @NotNull
    private DiscountType discountType;

    @Positive
    @NotNull
    private BigDecimal discountAmount;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "UTC")
    private Timestamp startDate;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH", timezone = "UTC")
    private Timestamp endDate;

    private Long productId;

    private Long subProductId;
}
