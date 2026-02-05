package org.handler.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.handler.dto.request.UserRequestDto;
import org.handler.dto.response.UserResponseDto;
import org.handler.exception.EmailAlreadyExistsException;
import org.handler.exception.UserNotFoundException;
import org.handler.exception.UsernameAlreadyExistsException;
import org.handler.mapper.UserMapper;
import org.handler.model.Case;
import org.handler.model.User;
import org.handler.repository.UserRepository;
import org.handler.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        log.info("Creating user with username: {} and email: {}",
                userRequestDto.getUsername(), userRequestDto.getEmail());

        checkIfUsernameUnique(userRequestDto.getUsername(), null);
        checkIfEmailUnique(userRequestDto.getEmail(), null);

        encodePassword(userRequestDto);

        User user = new User();
        userMapper.toUser(userRequestDto, user);

        userRepository.save(user);

        log.info("User created successfully with username: {} and email: {}",
                userRequestDto.getUsername(), userRequestDto.getEmail());

        return userMapper.toUserResponseDto(user);
    }

    @Override
    public UserResponseDto getUserById(Long userId) {
        User user = findUserById(userId);

        return userMapper.toUserResponseDto(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(userMapper::toUserResponseDto)
                .toList();
    }

    @Override
    public UserResponseDto updateUser(Long userId, UserRequestDto userRequestDto) {
        User user = findUserById(userId);

        checkIfUsernameUnique(userRequestDto.getUsername(), userId);
        checkIfEmailUnique(userRequestDto.getEmail(), userId);

        encodePassword(userRequestDto);

        userMapper.toUser(userRequestDto, user);

        return userMapper.toUserResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Attempting to delete user with ID: {}", userId);

        User user = findUserById(userId);

        disassociateCasesFromUser(user);

        userRepository.delete(user);

        log.info("Successfully deleted user with ID: {}", userId);
    }

    public void validateUserExists(Long userId) {
        boolean exists = userRepository.existsById(userId);
        if (!exists) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    private void encodePassword(UserRequestDto userRequestDto) {
        userRequestDto.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
    }

    private void checkIfUsernameUnique(String username, Long userId) {
        User existingUser = userRepository.findByUsername(username).orElse(null);

        if (existingUser != null) {
            log.error("Username {} is already in use. Operation aborted.", username);
            if (userId == null || !existingUser.getId().equals(userId)) {
                throw new UsernameAlreadyExistsException("Username '" + username + "' is already taken.");
            }
        }
    }

    private void checkIfEmailUnique(String email, Long userId) {
        User existingUser = userRepository.findByEmail(email).orElse(null);

        if (existingUser != null) {
            log.error("Email {} is already in use. Operation aborted", email);
            if (userId == null || !existingUser.getId().equals(userId)) {
                throw new EmailAlreadyExistsException("Email already taken: " + email);
            }
        }
    }

    private void disassociateCasesFromUser(User user) {
        for (Case caseEntity : user.getCases()) {
            caseEntity.setUser(null);
        }
    }
}