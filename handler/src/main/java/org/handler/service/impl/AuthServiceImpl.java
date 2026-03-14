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
import org.handler.repository.UserRepository;
import org.handler.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto login(LoginRequestDto loginRequestDto) {
        log.info("Attempting login for username: {}", loginRequestDto.getUsername());

        User user = userRepository.findByUsername(loginRequestDto.getUsername())
                .orElseThrow(() -> new UserNotFoundException("Invalid username or password"));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new UserNotFoundException("Invalid username or password");
        }

        log.info("Login successful for username: {}", loginRequestDto.getUsername());
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public UserResponseDto register(UserRequestDto userRequestDto) {
        log.info("Registering user with username: {} and email: {}",
                userRequestDto.getUsername(), userRequestDto.getEmail());

        checkIfUsernameUnique(userRequestDto.getUsername());
        checkIfEmailUnique(userRequestDto.getEmail());

        encodePassword(userRequestDto);

        User user = new User();
        userMapper.toUser(userRequestDto, user);

        User savedUser = userRepository.save(user);

        log.info("User registered successfully with username: {} and email: {}",
                userRequestDto.getUsername(), userRequestDto.getEmail());

        return userMapper.toUserResponseDto(savedUser);
    }

    
    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }
    
    @Override
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }
    
    private void encodePassword(UserRequestDto userRequestDto) {
        userRequestDto.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
    }

    private void checkIfUsernameUnique(String username) {
        User existingUser = userRepository.findByUsername(username).orElse(null);

        if (existingUser != null) {
            log.error("Username {} is already in use. Registration aborted.", username);
            throw new UsernameAlreadyExistsException("Username '" + username + "' is already taken.");
        }
    }

    private void checkIfEmailUnique(String email) {
        User existingUser = userRepository.findByEmail(email).orElse(null);

        if (existingUser != null) {
            log.error("Email {} is already in use. Registration aborted", email);
            throw new EmailAlreadyExistsException("Email already taken: " + email);
        }
    }
}