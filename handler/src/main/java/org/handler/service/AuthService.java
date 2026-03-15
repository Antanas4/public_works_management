package org.handler.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.handler.dto.request.LoginRequestDto;
import org.handler.dto.request.UserRequestDto;
import org.handler.dto.response.UserResponseDto;
import org.handler.model.UserPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthService {
    UserResponseDto login(@RequestBody LoginRequestDto loginRequestDto, HttpServletRequest request, HttpServletResponse response);
    UserResponseDto register(UserRequestDto userRequestDto);
    UserResponseDto getCurrentUser(UserPrincipal principal);
    void logout(HttpServletRequest request);
}