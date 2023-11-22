package com.divtech.employee_management.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationRequest {

    @NotNull
    private String email;

    @NotNull
    private String password;
}
