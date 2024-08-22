package com.commerce.pal.backend.dto.auth;

import com.commerce.pal.backend.utils.enumValues.Channel;
import com.commerce.pal.backend.utils.enumValues.SocialMedia;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class OAuth2ReqDto {
    @NotNull
    private Channel channel;

    @NotNull
    private SocialMedia provider;

    @NotEmpty
    private String providerUserId;

    @Email
    private String email; //if facebook is created with phoneNumber

    @NotEmpty
    private String firstName;

    private String lastName;

    private String deviceId; //For android and ios

}

