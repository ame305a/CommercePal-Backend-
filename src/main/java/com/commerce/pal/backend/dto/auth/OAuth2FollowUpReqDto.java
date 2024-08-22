package com.commerce.pal.backend.dto.auth;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
public class OAuth2FollowUpReqDto {
    @Email
    private String email;

    @NotEmpty
    private String phoneNumber;
}

