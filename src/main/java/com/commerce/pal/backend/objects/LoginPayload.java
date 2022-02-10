package com.commerce.pal.backend.objects;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LoginPayload {
    @NotNull
    private String email;
    @NotNull
    private String password;
}
