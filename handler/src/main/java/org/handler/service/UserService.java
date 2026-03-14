package org.handler.service;

import jakarta.validation.constraints.NotBlank;
import org.handler.dto.request.UserRequestDto;
import org.handler.dto.response.UserResponseDto;
import org.handler.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    UserResponseDto createUser(UserRequestDto userRequestDto);

    UserResponseDto getUserById(Long id);

    List<UserResponseDto> getAllUsers();

    void deleteUser(Long id);

    User findUserById(Long userId);

    boolean usernameExists(String username);

    boolean emailExists(String email);

    Optional<User> findByUsername(@NotBlank(message = "Username cannot be blank") String username);
}
