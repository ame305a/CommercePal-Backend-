package com.commerce.pal.backend.models.product.promotion;

public enum PromoCodeStatus {
    PENDING_WAREHOUSE_APPROVAL,
    REJECTED_BY_WAREHOUSE,
    ACTIVE,
    CANCELED,
    EXPIRED,
    INACTIVE;
}