package com.divtech.employee_management.service;

import com.divtech.employee_management.constant.Role;
import com.divtech.employee_management.util.JwtUtil;
import com.divtech.employee_management.domain.User;
import com.divtech.employee_management.dto.AuthenticationRequest;
import com.divtech.employee_management.dto.AuthenticationResponse;
import com.divtech.employee_management.dto.UserDTO;
import com.divtech.employee_management.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public UserDTO addUser(final UserDTO userDto, final String jwt){
        if (!JwtUtil.getRole(jwt).equals(Role.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "only admin can access");
        }
        User user = dtoToUser(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userDto.setId(userRepository.save(user).getId());
        userDto.setPassword(null);
        return userDto;
    }

    private User dtoToUser(UserDTO userDto) {
        User user = new User();
        user.setUserName(userDto.getUserName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        user.setRole(userDto.getRole());
        return user;
    }

    public AuthenticationResponse authenticate(final AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getEmail(),authenticationRequest.getPassword()));
        User user = userRepository.findByEmail(authenticationRequest.getEmail()).orElseThrow();
        String jwtToken = jwtUtil.generateToken(user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }
}
