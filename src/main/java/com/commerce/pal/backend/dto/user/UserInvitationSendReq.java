package com.commerce.pal.backend.dto.user;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserInvitationSendReq {

    @NotEmpty
    @Pattern(regexp = ".{9,}", message = "Phone number must be at least 9 characters long")
    private String phoneNumber;

    @NotEmpty
    private String appLink;
}
