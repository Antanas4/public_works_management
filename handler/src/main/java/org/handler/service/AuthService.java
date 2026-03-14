package org.handler.service;

import org.handler.dto.request.LoginRequestDto;
import org.handler.dto.request.UserRequestDto;
import org.handler.dto.response.UserResponseDto;

public interface AuthService {
    UserResponseDto login(LoginRequestDto loginRequestDto);
    UserResponseDto register(UserRequestDto userRequestDto);
}