package com.divtech.employee_management.dto;

import com.divtech.employee_management.constant.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private Long id;

    @NotNull
    private String userName;

    private String password;

    @NotNull
    private String email;

    @NotNull
    private Role role;
}
