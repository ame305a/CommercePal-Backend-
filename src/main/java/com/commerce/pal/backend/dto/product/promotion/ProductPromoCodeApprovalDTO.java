package com.commerce.pal.backend.dto.product.promotion;

import com.commerce.pal.backend.models.product.promotion.PromoCodeStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ProductPromoCodeApprovalDTO {
    @NotNull
    private PromoCodeStatus promoCodeStatus;
}
