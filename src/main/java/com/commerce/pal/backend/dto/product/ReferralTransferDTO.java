package com.commerce.pal.backend.dto.product;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Setter
@Getter
public class ReferralTransferDTO {

    @NotBlank(message = "Phone or email is required")
    private String phoneOrEmail;

    @NotNull(message = "Points to transfer is required")
    @Min(value = 1, message = "Points to transfer must be at least 1")
    private Integer pointsToTransfer;

}

