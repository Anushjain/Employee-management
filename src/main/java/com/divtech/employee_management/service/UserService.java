package com.divtech.employee_management.service;

import com.divtech.employee_management.dto.AuthenticationRequest;
import com.divtech.employee_management.dto.AuthenticationResponse;
import com.divtech.employee_management.dto.UserDTO;

public interface UserService {

    UserDTO addUser(final UserDTO userDto, final String token);

    AuthenticationResponse authenticate(final AuthenticationRequest authenticationRequest);

}
