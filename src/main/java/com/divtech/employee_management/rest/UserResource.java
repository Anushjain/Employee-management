package com.divtech.employee_management.rest;

import com.divtech.employee_management.dto.AuthenticationRequest;
import com.divtech.employee_management.dto.AuthenticationResponse;
import com.divtech.employee_management.dto.UserDTO;
import com.divtech.employee_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserResource {
    private final UserService userService;

    @PostMapping("/add")
    public ResponseEntity<UserDTO> addUser(@RequestBody UserDTO userDto,
                                           @RequestHeader(name="Authorization", required = false) final String token) {
        return ResponseEntity.ok(userService.addUser(userDto, token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> signIn(@RequestBody
                                                             AuthenticationRequest authenticationRequest) {
        return ResponseEntity.ok(userService.authenticate(authenticationRequest));
    }
}
