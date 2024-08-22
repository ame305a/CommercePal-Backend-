package com.commerce.pal.backend.dto.product.promotion;

import com.commerce.pal.backend.models.product.promotion.ProductPromoCodeOwner;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ProductPromoCodeCancelDTO {

    @NotNull
    private ProductPromoCodeOwner owner;
}
