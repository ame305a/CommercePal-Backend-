package com.commerce.pal.backend.dto.product.promotion;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;


@Data
public class ApplyPromoCodeDTO {

    @NotBlank
    private String promoCode;

    @Size(min = 1, message = "At least one product must be provided.")
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "Product ID is required.")
        private Long productId;

        @NotNull(message = "Sub-product ID is required.")
        private Long subProductId;

        @NotNull(message = "Quantity is required.")
        private Integer quantity;
    }
}
