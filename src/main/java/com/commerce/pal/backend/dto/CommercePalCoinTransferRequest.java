package com.commerce.pal.backend.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class CommercePalCoinTransferRequest {

    @NotEmpty(message = "Receiver username must not be empty")
    private String receiverUser;

    @NotNull(message = "Amount must not be null")
    private BigDecimal amount;
}
