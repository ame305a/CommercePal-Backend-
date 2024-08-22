package com.commerce.pal.backend.dto.user;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
public class UserInvitationReq {

    @NotEmpty(message = "Phone numbers list cannot be empty")
    private Set<String> phoneNumbers;
}
