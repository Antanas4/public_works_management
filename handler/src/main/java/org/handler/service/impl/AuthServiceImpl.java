package org.handler.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.handler.dto.request.LoginRequestDto;
import org.handler.dto.request.UserRequestDto;
import org.handler.dto.response.UserResponseDto;
import org.handler.exception.EmailAlreadyExistsException;
import org.handler.exception.UserNotFoundException;
import org.handler.exception.UsernameAlreadyExistsException;
import org.handler.mapper.UserMapper;
import org.handler.model.User;
import org.handler.model.UserPrincipal;
import org.handler.service.AuthService;
import org.handler.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;

    @Override
    public UserResponseDto login(LoginRequestDto loginRequestDto) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequestDto.getUsername(),
                                loginRequestDto.getPassword()
                        )
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return userMapper.toUserResponseDto(principal.getUser());
    }

    @Override
    public UserResponseDto register(UserRequestDto userRequestDto) {
        if (userService.usernameExists(userRequestDto.getUsername())) {
            throw new UsernameAlreadyExistsException(
                    "Username '" + userRequestDto.getUsername() + "' is already taken");
        }

        if (userService.emailExists(userRequestDto.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email '" + userRequestDto.getEmail() + "' is already registered");
        }

        encodePassword(userRequestDto);

        return userService.createUser(userRequestDto);
    }
    
    private void encodePassword(UserRequestDto userRequestDto) {
        userRequestDto.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
    }
}