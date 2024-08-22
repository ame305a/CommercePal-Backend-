package com.commerce.pal.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserInvitationResponse {
    private String phoneNumber;
    private boolean exists;
    private boolean alreadyInvited;
}
