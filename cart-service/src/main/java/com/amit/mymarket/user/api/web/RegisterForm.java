package com.amit.mymarket.user.api.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterForm(
    @NotBlank
    String username,

    @NotBlank
    @Size(min = 4, max = 64)
    String password,

    @NotBlank
    String confirmPassword) {

    public static RegisterForm EMPTY() {
        return new RegisterForm(null, null, null);
    }

}
